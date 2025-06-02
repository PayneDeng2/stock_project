@Repository
public interface StopLossTakeProfitRuleRepository extends JpaRepository<StopLossTakeProfitRule, Long> {
    List<StopLossTakeProfitRule> findByPositionId(Long positionId);
}