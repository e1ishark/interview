package task1;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Класс представляет собой контракт на реализацию сервиса сверок.
 * <p>
 * В финансовых сервисах возникает задача сверки операций на стороне контрагента (КА) и организации.
 * Сверка операций помогает сторонам найти ошибки и избежать разногласий по сумме взаиморасчётов.
 * Данные на стороне КА сравниваются с данными на стороне организации, все расхождения фиксируются для ручного разбора.
 */
public class ReconciliationSimple {

    /**
     * Сравнивает два списка и возвращает операции, по которым нашли расхождения
     * Возможны ситуации:
     * 1. Операция из списка операций на стороне КА не найдена в нашем списке - сохраняем операцию КА для дальнейшего разбора
     * 2. Операция из нашего списка не найдена на стороне КА - сохраняем нашу операцию для дальнейшего разбора
     * 3. Данные по одной и той же операции на стороне КА отличаются от данных на нашей стороне - сохраняем операцию КА для дальнейшего разбора
     *
     * @param ourOperations          список операции на нашей стороне
     * @param counterpartyOperations список операций на стороне контрагента
     * @return информация о расхождениях
     */
    Map<OperationDiffType, List<Operation>> checkOperations(
            List<Operation> ourOperations,
            List<Operation> counterpartyOperations
    ) {
        final Map<OperationDiffType, List<Operation>> statistics = new HashMap<>();

        List<Operation> operationsNotFoundInOur = new ArrayList<>(counterpartyOperations);
        operationsNotFoundInOur.removeIf(ourOperations::contains);
        statistics.put(OperationDiffType.notFoundInOurOperations, operationsNotFoundInOur);

        List<Operation> operationsNotFoundInCounterparty = new ArrayList<>(ourOperations);
        operationsNotFoundInCounterparty.removeIf(counterpartyOperations::contains);
        statistics.put(OperationDiffType.notFoundInCounterpartyOperations, operationsNotFoundInCounterparty);

        List<Operation> counterpartyOperationsWithDiff = new ArrayList<>(counterpartyOperations);
        List<Short> ourOperationIds = ourOperations.stream()
            .map(oo -> oo.id)
            .collect(Collectors.toList());
        counterpartyOperationsWithDiff.removeIf(co -> ourOperationIds.contains(co.id) && !ourOperations.contains(co));
        statistics.put(OperationDiffType.importantFieldsMismatch, counterpartyOperationsWithDiff);
        return statistics;
    }

    /**
     * Тип расхождения сверки
     */
    enum OperationDiffType {
        /**
         * Операция на найдена среди операций КА
         */
        notFoundInCounterpartyOperations,
        /**
         * Операция не найдена среди операций на нашей стороне
         */
        notFoundInOurOperations,
        /**
         * Данные операции на нашей стороне отличаются от данных операции на стороне КА
         */
        importantFieldsMismatch;
    }

    /**
     * Тип операции
     */
    enum OperationType {
        /**
         * Платеж
         */
        payment,
        /**
         * Возврат
         */
        refund;
    }

    /**
     * Валюта
     */
    enum Currency {
        /**
         * Российский рубль
         */
        rub,
        /**
         * Доллар США
         */
        usd,
        /**
         * Евро
         */
        eur;
    }

    /**
     * Данные операции
     */
    class Operation {
        /**
         * Уникальный идентификатор операции
         */
        short id;
        /**
         * Тип операции
         */
        OperationType operationType;
        /**
         * Сумма операции
         */
        BigDecimal amount;
        /**
         * Валюта операции
         */
        Currency currency;
        /**
         * Время создания операции
         */
        ZonedDateTime createdAt;

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof Operation)) {
                return false;
            }
            final Operation operation = (Operation) o;
            return id == operation.id
                && operationType == operation.operationType
                && (amount == null ? operation.amount == null
                : amount.compareTo(operation.amount) == 0)
                && currency == operation.currency
                && Objects.equals(createdAt, operation.createdAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

}