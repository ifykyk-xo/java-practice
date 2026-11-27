
interface CashbackCapable {
    double MAX_CASHBACK_UAH = 500.0;
    double getCashbackRate();
    default double calculateCashback(double baseAmount) {
        return Math.min(MAX_CASHBACK_UAH, baseAmount * getCashbackRate());
    }
}
interface Refundable {
    boolean requestRefund(String reason);
    boolean isRefunded();
}
interface BlockchainVerifiable {
    String getTxHash();
    int getConfirmations();
    default boolean isFinalized() {
        return getConfirmations() >= 12;
    }
}
abstract class AbstractPaymentTransaction {
    private final String txId;
    private final double baseAmountUah;
    private boolean processed;
    public AbstractPaymentTransaction(String txId, double baseAmountUah) {
        this.txId = txId;
        this.baseAmountUah = baseAmountUah;
        this.processed = false;
    }
    public abstract double calculateFee();
    public abstract String getPaymentChannel();
    public final boolean process() {
        if (baseAmountUah <= 0 || processed) {
            return false;
        }
        processed = true;
        return true;
    }
    public final double getTotalCharged() {
        return baseAmountUah + calculateFee();
    }
    public String getTxId() {
        return txId;
    }
    public double getBaseAmountUah() {
        return baseAmountUah;
    }
    public boolean isProcessed() {
        return processed;
    }
}
class PremiumCardPayment extends AbstractPaymentTransaction
        implements CashbackCapable, Refundable {
    private final String maskedPan;
    private final double cashbackRate;
    private boolean refunded;
    public PremiumCardPayment(String txId, double amount, String maskedPan) {

        super(txId, amount);
        this.maskedPan = maskedPan;
        this.cashbackRate = 0.03;
        this.refunded = false;
    }
    @Override
    public double calculateFee() {
        return getBaseAmountUah() * 0.01;
    }
    @Override
    public String getPaymentChannel() {
        return "Visa Infinite";
    }
    @Override
    public double getCashbackRate() {
        return cashbackRate;
    }
    @Override
    public boolean requestRefund(String reason) {
        if (isProcessed() && !refunded) {
            refunded = true;
            return true;
        }

        return false;
    }
    @Override
    public boolean isRefunded() {
        return refunded;
    }
}
class CryptoUsdtPayment extends AbstractPaymentTransaction
        implements CashbackCapable, BlockchainVerifiable {
    private final String txHash;
    private final int confirmations;
    public CryptoUsdtPayment(String txId, double amount,
                             String txHash, int confirmations) {
        super(txId, amount);

        this.txHash = txHash;
        this.confirmations = confirmations;
    }
    @Override
    public double calculateFee() {
        return 40.0;
    }
    @Override
    public String getPaymentChannel() {
        return "USDT TRC-20";
    }
    @Override
    public double getCashbackRate() {
        return 0.01;
    }
    @Override
    public String getTxHash() {
        return txHash;
    }
    @Override
    public int getConfirmations() {
        return confirmations;
    }
}
final class TaxTreasuryPayment extends AbstractPaymentTransaction
        implements Refundable {
    private final String taxCode;
    private boolean refunded;
    public TaxTreasuryPayment(String txId, double amount,
                              String taxCode) {
        super(txId, amount);

        this.taxCode = taxCode;
        this.refunded = false;
    }
    @Override
    public double calculateFee() {
        return 0.0;
    }
    @Override
    public String getPaymentChannel() {
        return "ДержКазначейство";
    }
    @Override
    public boolean requestRefund(String reason) {
        if (isProcessed() && !refunded) {
            refunded = true;
            return true;
        }

        return false;
    }
    @Override
    public boolean isRefunded() {
        return refunded;
    }
}
public class Task2SmartPaymentAndAuditGateway {
    public static String format2(double value) {
        long whole = (long) value;
        long cents = Math.round((value - whole) * 100);
        if (cents < 10) {
            return whole + ".0" + cents;
        }
        return whole + "." + cents;
    }
    public static void financialAudit(AbstractPaymentTransaction[] ledger) {
        double totalTurnover = 0;
        double totalFees = 0;
        double totalCashback = 0;
        System.out.println("\n[1. ПРОВЕДЕННЯ ПАКЕТУ ТРАНЗАКЦІЙ (final process())]");
        for (AbstractPaymentTransaction tx : ledger) {
            if (tx.process()) {
                System.out.println("  ✅ [" + tx.getTxId() + " | "
                        + tx.getPaymentChannel() + "] Сума: "
                        + format2(tx.getBaseAmountUah()) + " грн | Комісія: "
                        + format2(tx.calculateFee()) + " грн | Всього: "
                        + format2(tx.getTotalCharged()) + " грн");
                totalTurnover += tx.getBaseAmountUah();
                totalFees += tx.calculateFee();
            }
        }
        System.out.println("\n[2. АВТОМАТИЧНИЙ АУДИТ МОЖЛИВОСТЕЙ (Pattern Matching instanceof)]");
        boolean refundTestDone = false;
        for (AbstractPaymentTransaction tx : ledger) {
            if (tx instanceof CashbackCapable cashback) {
                double cashbackAmount = cashback.calculateCashback(
                        tx.getBaseAmountUah()
                );
                totalCashback += cashbackAmount;
                if (tx instanceof PremiumCardPayment) {
                    double calculated = tx.getBaseAmountUah()
                            * cashback.getCashbackRate();
                    System.out.println("  💰 [CashbackCapable] " + tx.getTxId()
                            + ": розрахунковий 3% = "
                            + format2(calculated) + " грн -> нараховано "
                            + format2(cashbackAmount)
                            + " грн (Ліміт MAX_CASHBACK_UAH!)");
                } else {
                    System.out.println("  💰 [CashbackCapable] " + tx.getTxId()
                            + ": нараховано 1% крипто-бонусу = "
                            + format2(cashbackAmount) + " грн");
                }
            }
            if (tx instanceof BlockchainVerifiable blockchain) {
                System.out.println("  ⛓️ [BlockchainVerifiable] " + tx.getTxId()
                        + ": Hash=" + blockchain.getTxHash()
                        + ", Підтверджень=" + blockchain.getConfirmations()
                        + " (Finalized: " + blockchain.isFinalized() + ")");
            }
            if (tx instanceof Refundable refundable && !refundTestDone) {
                if (refundable.requestRefund("Подвійне списання")) {
                    System.out.println("  🔄 [Refundable] Спроба повернення "
                            + tx.getTxId()
                            + " ('Подвійне списання'): УСПІШНО ПОВЕРНЕНО!");

                    refundTestDone = true;
                }
            }
        }
        System.out.println("\n[ПІДСУМКОВИЙ ЗВІТ ШЛЮЗУ]");
        System.out.println("  Сумарний базовий оборот: "
                + format2(totalTurnover) + " грн");
        System.out.println("  Сумарна комісія шлюзу: "
                + format2(totalFees) + " грн");
        System.out.println("  Сумарний виплачений кешбек: "
                + format2(totalCashback) + " грн");
        System.out.println("=========================================================================================");
    }
    public static void main(String[] args) {
        System.out.println("================ ПРОЦЕСИНГОВИЙ ЦЕНТР FINTECH (POLYMORPHISM & INTERFACES) ================");
        AbstractPaymentTransaction[] ledger = {
                new PremiumCardPayment(
                        "TX-101", 20000.0, "**** **** **** 1234"
                ),
                new CryptoUsdtPayment(
                        "TX-102", 8500.0, "0x9f8a...c4", 18
                ),
                new TaxTreasuryPayment(
                        "TX-103", 5000.0, "1234567890"
                )
        };
        financialAudit(ledger);
    }
}
