class BankAccount {
    private String iban;
    private String ownerName;
    protected double balance;
    public BankAccount(String iban, String ownerName, double initialBalance) {
        this.iban = iban;
        this.ownerName = ownerName;
        if (initialBalance < 0) {
            this.balance = 0.0;
        } else {
            this.balance = initialBalance;
        }
    }
    public String getIban() {
        return iban;
    }
    public String getOwnerName() {
        return ownerName;
    }
    public double getBalance() {
        return balance;
    }
    public void deposit(double amount) {
        if (amount > 0) {
            balance = balance + amount;
        }
    }
    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance = balance - amount;
            return true;
        } else {
            System.out.println("Недостатньо коштів!");
            return false;
        }
    }
    @Override
    public String toString() {
        return "Рахунок[" + iban +
                " | Власник: " + ownerName +
                " | Баланс: " + (Math.round(balance * 100.0) / 100.0) + " грн]";
    }
}
class SavingsAccount extends BankAccount {
    private double interestRate;
    private double minBalance;
    public SavingsAccount(String iban, String ownerName, double initialBalance,
                          double interestRate, double minBalance) {
        super(iban, ownerName, initialBalance);
        this.interestRate = interestRate;
        this.minBalance = minBalance;
    }
    @Override
    public boolean withdraw(double amount) {
        if (amount > 0 && balance - amount >= minBalance) {
            return super.withdraw(amount);
        } else {
            System.out.println("Відмова! Неможливо зняти гроші нижче мінімального залишка.");
            return false;
        }
    }
    public void applyInterest() {
        double interest = balance * (interestRate / 100.0);
        deposit(interest);
    }
    @Override
    public String toString() {
        return "SavingsAccount{" + super.toString() +
                ", Ставка: " + interestRate +
                "%, Мін.залишок: " + (Math.round(minBalance * 100.0) / 100.0) + " грн}";
    }
}
class CreditAccount extends BankAccount {
    private double creditLimit;
    private double feePercent;

    public CreditAccount(String iban, String ownerName, double initialBalance,
                         double creditLimit, double feePercent) {
        super(iban, ownerName, initialBalance);
        this.creditLimit = creditLimit;
        this.feePercent = feePercent;
    }
    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Некоректна сума!");
            return false;
        }
        if (balance >= amount) {
            return super.withdraw(amount);
        } else {
            double borrowed = amount - Math.max(0.0, balance);
            double fee = borrowed * (feePercent / 100.0);

            if (balance - amount - fee >= -creditLimit) {
                balance = balance - amount - fee;
                return true;
            } else {
                System.out.println("Перевищен кредитний ліміт!");
                return false;
            }
        }
    }
    public double getCurrentDebt() {
        if (balance < 0) {
            return Math.abs(balance);
        } else {
            return 0.0;
        }
    }
    @Override
    public String toString() {
        return "CreditAccount{" + super.toString() +
                ", Ліміт: " + (Math.round(creditLimit * 100.0) / 100.0) +
                " грн, Борг: " + (Math.round(getCurrentDebt() * 100.0) / 100.0) + " грн}";
    }
}
public class Task2BankingHierarchy {
    public static void main(String[] args) {

        System.out.println("================ БАНКІВСЬКА ІЄРАРХІЯ РАХУНКІВ ================");

        SavingsAccount savings = new SavingsAccount(
                "UA101", "Олена Коваль", 5000, 12.0, 1000
        );
        System.out.println("1. Ощадний рахунок (початковий стан):");
        System.out.println(savings);

        boolean savingsResult = savings.withdraw(4500);

        if (savingsResult) {
            System.out.println("Списання успішне!");
        } else {
            System.out.println("Списання відхилено!");
        }
        System.out.println("Начислення річний процентів (+12%)...");
        savings.applyInterest();
        System.out.println(savings);
        System.out.println();
        CreditAccount credit = new CreditAccount(
                "UA202", "Максим Бондар", 2000, 10000, 2.0
        );
        System.out.println("2. Кредитний рахунок (початковий стан):");
        System.out.println(credit);
        boolean creditResult = credit.withdraw(5000);
        if (creditResult) {
            System.out.println("Покупка на 5000 грн пройшла успішно!");
        } else {
            System.out.println("Покупка відхилена!");
        }
        System.out.println(credit);
        System.out.println("==============================================================");
    }
}
