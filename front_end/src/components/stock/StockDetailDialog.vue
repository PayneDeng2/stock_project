<template>
  <teleport to="body">    <el-dialog
      :model-value="visible"
      @update:model-value="$emit('update:visible', $event)"
      :title="stock && stock.name ? `${stock.name} (${stock.code})` : '股票详情'"
      :width="dialogWidth"
      class="stock-detail-dialog"
      :before-close="handleClose"
      append-to-body
      destroy-on-close
    >
    <div class="stock-detail-content">      <div class="price-overview">
        <div class="current-price">
          <span class="price">{{ stock && stock.currentPrice || '--' }}</span>
          <span v-if="stock && stock.change !== undefined" :class="{'change-up': stock.change > 0, 'change-down': stock.change < 0}" class="change">
            {{ stock.change > 0 ? '+' : '' }}{{ stock.change }}%
          </span>
          <span v-else class="change">--</span>
        </div>
        <div class="trading-date">
          {{ getTodayDate() }} 交易时段
        </div>
      </div>

      <el-descriptions class="info-table" title="基本信息" :column="2" border>
        <el-descriptions-item label="股票代码">{{ stock.code }}</el-descriptions-item>
        <el-descriptions-item label="股票名称">{{ stock.name }}</el-descriptions-item>
        <el-descriptions-item label="今日开盘价">{{ stock.openPrice || '--' }} 元</el-descriptions-item>
        <el-descriptions-item label="昨日收盘价">{{ stock.prevClosePrice || '--' }} 元</el-descriptions-item>
        <el-descriptions-item label="最高价">{{ stock.highPrice || '--' }} 元</el-descriptions-item>
        <el-descriptions-item label="最低价">{{ stock.lowPrice || '--' }} 元</el-descriptions-item>
        <el-descriptions-item label="成交量">{{ stock.volume || '--' }}</el-descriptions-item>
        <el-descriptions-item label="成交额">{{ stock.turnover || '--' }}</el-descriptions-item>
        <el-descriptions-item label="市盈率">{{ stock.pe || '--' }}</el-descriptions-item>
        <el-descriptions-item label="市净率">{{ stock.pb || '--' }}</el-descriptions-item>
      </el-descriptions>

      <div class="chart-section">
        <div class="section-header">
          <h3>股价走势</h3>
          <div class="time-selector">
            <el-radio-group v-model="timeRange" size="small">
              <el-radio-button label="day">日</el-radio-button>
              <el-radio-button label="week">周</el-radio-button>
              <el-radio-button label="month">月</el-radio-button>
              <el-radio-button label="year">年</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        <div class="price-chart">
          <div ref="chartDomRef" style="width: 100%; height: 100%;"></div>
        </div>
      </div>      <div class="action-section">
        <el-button type="primary" class="action-button trade-button" @click="goToTrading">
          <el-icon><Sell /></el-icon>前往交易
        </el-button>
        <el-button class="action-button" @click="showAlertDialog">
          <el-icon><Bell /></el-icon>设置提醒
        </el-button>
        <el-button 
          class="action-button" 
          :class="{ 'in-watchlist': isInWatchlist }"
          @click="toggleWatchlist"
        >
          <el-icon><Star /></el-icon>{{ isInWatchlist ? '移出自选' : '加入自选' }}
        </el-button>      </div>
    </div>

    <!-- 股票提醒弹窗 -->
    <stock-alert-dialog 
      v-model:visible="alertDialogVisible" 
      :stock="stock"
      @alert-added="handleAlertAdded"
    />
      </el-dialog>
  </teleport>
</template>

<script>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { Sell, Bell, Star } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import StockAlertDialog from './StockAlertDialog.vue';
import { addToWatchlist, removeFromWatchlist, isInWatchlist as checkInWatchlist } from '@/utils/stockManager';
import * as echarts from 'echarts/core'; // 引入 ECharts核心
import { LineChart } from 'echarts/charts'; // 引入折线图
import { TitleComponent, TooltipComponent, GridComponent, LegendComponent, DataZoomComponent } from 'echarts/components'; // 引入必要的组件
import { CanvasRenderer } from 'echarts/renderers'; // 引入渲染器
import { getStockHistoricalData } from '@/utils/stockDataService';

// 注册 ECharts 组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent,
  LineChart,
  CanvasRenderer
]);

export default {
  name: 'StockDetailDialog',
  components: {
    Sell,
    Bell,
    Star,
    StockAlertDialog
  },props: {
    visible: {
      type: Boolean,
      required: true,
      default: false
    },
    stock: {
      type: Object,
      required: true,
      default: () => ({})
    }
  },
  setup(props, { emit }) {
    const router = useRouter();
    const timeRange = ref('day'); // 时间范围选择，例如 'day', 'week', 'month', 'year'
    const alertDialogVisible = ref(false);
    const isInWatchlist = ref(false);

    const chartDomRef = ref(null); // 图表DOM元素的引用
    let chartInstance = null; // ECharts 实例
    const chartData = ref([]); // 存储图表数据

    const dialogWidth = computed(() => {
      if (typeof window !== 'undefined') {
        const width = window.innerWidth;
        if (width <= 480) return '98%';
        if (width <= 768) return '95%';
        return '60%';
      }
      return '60%';
    });

    const getTodayDate = () => {
      const today = new Date();
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, '0');
      const day = String(today.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    };

    // 获取并更新图表数据
    const fetchChartData = async () => {
      if (!props.stock || !props.stock.code || !chartInstance) {
        // 如果股票信息不完整或图表未初始化，则不执行
        if (chartInstance) chartInstance.clear(); // 清除旧数据
        return;
      }
      try {
        chartInstance.showLoading(); // 显示加载动画
        // 调用您的服务中的新函数
        const historicalData = await getStockHistoricalData(props.stock.code, timeRange.value);
        chartData.value = historicalData;
        updateChart();
      } catch (error) {
        console.error('获取股价走势数据失败:', error);
        ElMessage.error('获取股价走势失败');
        chartData.value = []; // 出错时清空数据
        updateChart(); // 更新图表以显示空状态或错误信息
      } finally {
        if (chartInstance && !chartInstance.isDisposed()) {
           chartInstance.hideLoading(); // 隐藏加载动画
        }
      }
    };

    // 初始化或更新图表
    const updateChart = () => {
      if (!chartInstance || chartInstance.isDisposed()) return;
      const option = {
        tooltip: {
          trigger: 'axis', // 坐标轴触发
          axisPointer: {
            type: 'cross' // 十字准星指示器
          }
        },
        xAxis: {
          type: 'category', // 类目轴，适用于离散的标签，如日期或时间点
          data: chartData.value.map(item => item.time), // 例如: ['09:30', '10:00', ...] 或日期
          boundaryGap: false, // x轴数据点是否在刻度中间
        },
        yAxis: {
          type: 'value', // 数值轴
          scale: true, // Y轴会自动缩放以适应数据
          axisLabel: {
            formatter: '{value}' // 可以添加单位，如 '{value} 元'
          }
        },
        dataZoom: [ // 可选：添加数据区域缩放功能，方便大数据量查看
            {
                type: 'inside', // 内置型数据区域缩放组件
                start: 0,
                end: 100
            },
            {
                type: 'slider', // 滑动条型数据区域缩放组件
                start: 0,
                end: 100,
                bottom: '2%' // 调整位置，避免与X轴标签重叠
            }
        ],
        series: [
          {
            name: props.stock.name || '价格',
            type: 'line', // 折线图
            data: chartData.value.map(item => item.price), // 例如: [10.2, 10.5, ...]
            smooth: true, // 可选：平滑曲线显示
            showSymbol: chartData.value.length < 50, // 数据点较少时显示标记点
            lineStyle: {
                width: 2 // 线条宽度
            },
            // 根据涨跌给线条不同颜色
            itemStyle: {
              color: props.stock.change > 0 ? '#f56c6c' : (props.stock.change < 0 ? '#67c23a' : '#5470c6')
            }
          }
        ],
        grid: { // 调整网格位置，防止标签被裁剪
            left: '3%',  // 适当调整以显示Y轴标签
            right: '4%',
            bottom: '10%', // 为 dataZoom 留出空间
            containLabel: true // grid 区域是否包含坐标轴的刻度标签
        }
      };
      chartInstance.setOption(option, true); // true 表示不与之前的 option 合并
    };

    // 监听弹窗可见性
    watch(() => props.visible, (newVal) => {
      if (newVal) {
        nextTick(() => { // 确保DOM已渲染
          if (chartDomRef.value && (!chartInstance || chartInstance.isDisposed())) {
            chartInstance = echarts.init(chartDomRef.value);
            // 添加窗口大小变化监听，用于图表自适应
            window.addEventListener('resize', resizeChartHandler);
          }
          if (chartInstance && !chartInstance.isDisposed()){
             chartInstance.resize(); // 每次显示时尝试调整大小
             fetchChartData(); // 弹窗可见时获取数据
          }
        });
      } else {
        // 弹窗关闭时可以考虑移除resize监听器，虽然onBeforeUnmount中也会处理
        if (chartInstance && !chartInstance.isDisposed()) {
          chartInstance.dispose(); // 销毁图表实例，防止内存泄漏
          chartInstance = null;
          window.removeEventListener('resize', resizeChartHandler);
        }
      }
    });

    const resizeChartHandler = () => {
        if (chartInstance && !chartInstance.isDisposed()) {
            chartInstance.resize();
        }
    };

    // 监听股票变化
    watch(() => props.stock, (newStock) => {
      if (newStock && newStock.code) {
        isInWatchlist.value = checkInWatchlist(newStock.code);
        if (props.visible && chartInstance && !chartInstance.isDisposed()) {
          fetchChartData(); // 股票变化且弹窗可见时，重新获取图表数据
        }
      }
    }, { immediate: true, deep: true }); // immediate: 初始执行, deep: 深度监听

    // 监听时间范围变化
    watch(timeRange, () => {
      if (props.visible && chartInstance && !chartInstance.isDisposed()) {
        fetchChartData(); // 时间范围变化时，重新获取图表数据
      }
    });

    onMounted(() => {
        // 组件挂载时，如果弹窗初始就可见，则初始化图表
        if (props.visible && chartDomRef.value && (!chartInstance || chartInstance.isDisposed())) {
            nextTick(() => { // 确保DOM元素已准备好
              console.log(chartDomRef.value);
              chartInstance = echarts.init(chartDomRef.value);
              window.addEventListener('resize', resizeChartHandler);
              fetchChartData();
            });
        }
    });

    onBeforeUnmount(() => {
      window.removeEventListener('resize', resizeChartHandler); // 移除resize监听
      if (chartInstance && !chartInstance.isDisposed()) {
        chartInstance.dispose(); // 销毁图表实例，防止内存泄漏
        chartInstance = null;
      }
    });

    // 前往交易页面
    const goToTrading = () => {
      if (!props.stock || !props.stock.code) {
        ElMessage.warning('股票信息不完整');
        return;
      }

      // 关闭弹窗
      emit('update:visible', false);
      
      // 将股票信息存储到sessionStorage，供交易页面使用
      sessionStorage.setItem('selectedStockForTrade', JSON.stringify({
        code: props.stock.code,
        name: props.stock.name,
        currentPrice: props.stock.currentPrice
      }));

      // 跳转到交易页面
      router.push('/trading');
      ElMessage.success(`已跳转到交易页面，自动选择${props.stock.name}`);
    };

    // 显示提醒设置弹窗
    const showAlertDialog = () => {
      if (!props.stock || !props.stock.code) {
        ElMessage.warning('股票信息不完整');
        return;
      }
      alertDialogVisible.value = true;
    };

    // 切换自选股状态
    const toggleWatchlist = () => {
      if (!props.stock || !props.stock.code) {
        ElMessage.warning('股票信息不完整');
        return;
      }

      if (isInWatchlist.value) {
        // 移出自选
        const success = removeFromWatchlist(props.stock.code);
        if (success) {
          isInWatchlist.value = false;
          ElMessage.success(`已移出自选股: ${props.stock.name}`);
          emit('watchlist-changed', 'remove', props.stock);
        } else {
          ElMessage.error('移出自选股失败');
        }
      } else {
        // 添加到自选
        const success = addToWatchlist(props.stock);
        if (success) {
          isInWatchlist.value = true;
          ElMessage.success(`已添加到自选股: ${props.stock.name}`);
          emit('watchlist-changed', 'add', props.stock);
        } else {
          ElMessage.error('添加自选股失败');
        }
      }
    };

    // 处理提醒添加完成
    const handleAlertAdded = (alertData) => {
      ElMessage.success(`已为 ${props.stock.name} 设置价格提醒`);
      emit('alert-added', alertData);
    };

    return {
      timeRange,
      dialogWidth,
      alertDialogVisible,
      isInWatchlist,
      getTodayDate,
      goToTrading,
      showAlertDialog,
      toggleWatchlist,
      handleAlertAdded,
      chartDomRef,
    };
  },
  emits: ['update:visible', 'close', 'watchlist-changed', 'alert-added'],
  methods: {
    handleClose() {
      this.$emit('update:visible', false);
      this.$emit('close');
    }
  }
};
</script>

<style scoped>
.stock-detail-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  margin: 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background-color: #f9f9f9;
}

.stock-detail-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.stock-detail-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.stock-detail-dialog :deep(.el-dialog__headerbtn) {
  top: 16px;
  right: 16px;
}

.stock-detail-content {
  padding: 0;
}

.price-overview {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.current-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.price {
  font-size: 36px;
  font-weight: 700;
  margin-right: 12px;
}

.change {
  font-size: 18px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.change-up {
  color: #f56c6c;
  background-color: rgba(245, 108, 108, 0.1);
}

.change-down {
  color: #67c23a;
  background-color: rgba(103, 194, 58, 0.1);
}

.trading-date {
  font-size: 14px;
  color: #909399;
}

.info-table {
  margin-bottom: 30px;
}

.info-table :deep(.el-descriptions__title) {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.info-table :deep(.el-descriptions__label) {
  font-weight: 500;
}

.chart-section {
  margin-top: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.time-selector {
  display: flex;
  align-items: center;
}

.price-chart {
  width: 100%;
  height: 300px;
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
}

.chart-placeholder {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.action-section {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  justify-content: center;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  transition: transform 0.2s, box-shadow 0.2s;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.trade-button {
  background-color: #0071e3;
  border-color: #0071e3;
}

.trade-button:hover {
  background-color: #0077ed;
  border-color: #0077ed;
}

.in-watchlist {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.in-watchlist:hover {
  background-color: #f78989;
  border-color: #f78989;
}

@media (max-width: 768px) {
  .stock-detail-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 5vh auto !important;
  }

  .stock-detail-dialog :deep(.el-dialog__header) {
    padding: 12px 16px;
  }

  .stock-detail-dialog :deep(.el-dialog__title) {
    font-size: 16px;
  }

  .stock-detail-dialog :deep(.el-dialog__body) {
    padding: 16px 20px;
  }

  .price-overview {
    margin-bottom: 20px;
    padding-bottom: 12px;
  }

  .price {
    font-size: 28px;
  }

  .change {
    font-size: 16px;
    padding: 2px 6px;
  }

  .trading-date {
    font-size: 13px;
  }

  .info-table {
    margin-bottom: 24px;
  }

  .info-table :deep(.el-descriptions__title) {
    font-size: 15px;
    margin-bottom: 12px;
  }

  .info-table :deep(.el-descriptions) {
    --el-descriptions-table-border: 1px solid #dcdfe6;
    --el-descriptions-item-bordered-label-background: #fafafa;
  }

  .info-table :deep(.el-descriptions__label) {
    font-size: 13px;
    padding: 8px 12px;
  }

  .info-table :deep(.el-descriptions__content) {
    font-size: 13px;
    padding: 8px 12px;
  }

  .chart-section {
    margin-top: 24px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 12px;
  }

  .section-header h3 {
    font-size: 15px;
  }

  .price-chart {
    height: 250px;
    margin-bottom: 24px;
  }

  .action-section {
    flex-direction: column;
    gap: 10px;
    margin-top: 16px;
  }

  .action-button {
    width: 100%;
    justify-content: center;
    padding: 12px 20px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .stock-detail-dialog :deep(.el-dialog) {
    width: 98% !important;
    margin: 2vh auto !important;
    border-radius: 8px;
  }

  .stock-detail-dialog :deep(.el-dialog__header) {
    padding: 10px 12px;
  }

  .stock-detail-dialog :deep(.el-dialog__title) {
    font-size: 15px;
  }

  .stock-detail-dialog :deep(.el-dialog__body) {
    padding: 12px 16px;
  }

  .price-overview {
    margin-bottom: 16px;
    padding-bottom: 10px;
  }

  .current-price {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .price {
    font-size: 24px;
    margin-right: 0;
  }

  .change {
    font-size: 14px;
    padding: 2px 5px;
  }

  .trading-date {
    font-size: 12px;
  }

  .info-table {
    margin-bottom: 20px;
  }

  .info-table :deep(.el-descriptions__title) {
    font-size: 14px;
    margin-bottom: 10px;
  }

  .info-table :deep(.el-descriptions__label) {
    font-size: 12px;
    padding: 6px 10px;
  }

  .info-table :deep(.el-descriptions__content) {
    font-size: 12px;
    padding: 6px 10px;
  }

  .chart-section {
    margin-top: 20px;
  }

  .section-header h3 {
    font-size: 14px;
  }

  .time-selector :deep(.el-radio-button__inner) {
    font-size: 12px;
    padding: 8px 12px;
  }

  .price-chart {
    height: 200px;
    margin-bottom: 20px;
  }

  .action-section {
    margin-top: 12px;
    gap: 8px;
  }

  .action-button {
    padding: 10px 16px;
    font-size: 13px;
  }
}
</style>
