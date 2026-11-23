import java.util.Scanner;

public class Task1FuelCalculator {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Завдання 1

        System.out.println("=== КАЛЬКУЛЯТОР ПОЇЗДКИ ТА ПАЛЬНОГО ===");

        System.out.print("Введіть відстань поїздки (км): ");
        double distance = scanner.nextDouble();

        System.out.print("Введіть середню витрату на 100 км (л): ");
        double fuelConsumption = scanner.nextDouble();

        System.out.print("Оберіть тип пального (1 - А-95, 2 - Дизель, 3 - Газ): ");
        int fuelType = scanner.nextInt();

        if (distance <= 0 || fuelConsumption <= 0) {
            System.out.println("Помилка: відстань та витрата повинні бути більшими за 0");
        } else {

            double pricePerLiter = 0;
            String fuelName = "";

            switch (fuelType) {
                case 1:
                    pricePerLiter = 56.50;
                    fuelName = "Бензин А-95";
                    break;
                case 2:
                    pricePerLiter = 52.80;
                    fuelName = "Дизель";
                    break;
                case 3:
                    pricePerLiter = 29.40;
                    fuelName = "Газ";
                    break;
                default:
                    System.out.println("Помилка: невідомий тип пального");
            }

            if (fuelType >= 1 && fuelType <= 3) {

                double neededFuel = distance * fuelConsumption / 100.0;
                neededFuel = Math.round(neededFuel * 100.0) / 100.0;

                double baseCost = neededFuel * pricePerLiter;
                baseCost = Math.round(baseCost * 100.0) / 100.0;

                double discount = (baseCost > 2000.0)
                        ? baseCost * 0.05
                        : 0;

                discount = Math.round(discount * 100.0) / 100.0;

                double finalCost = (baseCost > 2000.0)
                        ? baseCost * 0.95
                        : baseCost;

                finalCost = Math.round(finalCost * 100.0) / 100.0;

                System.out.println();
                System.out.println("---------------- ЗВІТ ПОЇЗДКИ ----------------");
                System.out.println("Обране пальне:               " + fuelName
                        + " (" + pricePerLiter + " грн/л)");
                System.out.println("Необхідно пального:          " + neededFuel + " л");
                System.out.println("Базова вартість:             " + baseCost + " грн");
                System.out.println("Знижка АЗС (5%):             " + discount + " грн");
                System.out.println("----------------------------------------------");
                System.out.println("Підсумкова сума до сплати:   " + finalCost + " грн");
                System.out.println("==============================================");
            }
        }

        scanner.close();
    }
}