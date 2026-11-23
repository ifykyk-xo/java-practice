

class Motherboard {
    private final String model;
    private final String socketType;
    private final int maxExpansionSlots;
    public Motherboard(String model, String socketType, int maxExpansionSlots) {
        this.model = model;
        this.socketType = socketType;
        if (maxExpansionSlots >= 1 && maxExpansionSlots <= 8) {
            this.maxExpansionSlots = maxExpansionSlots;
        } else {
            this.maxExpansionSlots = 1;
        }
    }
    public boolean isSocketCompatible(String cpuSocket) {
        return socketType.equalsIgnoreCase(cpuSocket);
    }
    public String getModel() {
        return model;
    }
    public String getSocketType() {
        return socketType;
    }
    public int getMaxExpansionSlots() {
        return maxExpansionSlots;
    }
}
class PowerSupplyUnit {
    private final String model;
    private final int maxOutputWatts;
    private final double efficiencyCoefficient;

    public PowerSupplyUnit(String model, int maxOutputWatts, double efficiencyCoefficient) {
        this.model = model;
        this.maxOutputWatts = maxOutputWatts;

        if (efficiencyCoefficient >= 0.80 && efficiencyCoefficient <= 0.96) {
            this.efficiencyCoefficient = efficiencyCoefficient;
        } else {
            this.efficiencyCoefficient = 0.80;
        }
    }
    public int getSafeContinuousWatts() {
        return (int) (maxOutputWatts * 0.85);
    }
    public double calculateWallPowerDraw(int internalLoadWatts) {
        return internalLoadWatts / efficiencyCoefficient;
    }
    public String getModel() {
        return model;
    }
    public double getEfficiencyCoefficient() {
        return efficiencyCoefficient;
    }
}
abstract class AbstractHardwareComponent {
    private final String modelName;
    private final double priceUah;
    public AbstractHardwareComponent(String modelName, double priceUah) {
        this.modelName = modelName;

        if (priceUah >= 0) {
            this.priceUah = priceUah;
        } else {
            this.priceUah = 0;
        }
    }
    public String getModelName() {
        return modelName;
    }
    public double getPriceUah() {
        return priceUah;
    }
    public abstract int getPowerWatts();
    public abstract double getBenchmarkScore();
    public abstract String getCategory();
    @Override
    public String toString() {
        return "[" + getCategory() + "] " + modelName +
                " (" + getPowerWatts() + " Вт | Score: " +
                (Math.round(getBenchmarkScore() * 100.0) / 100.0) + ")";
    }
}
class ProcessorUnit extends AbstractHardwareComponent {
    private final String socket;
    private final int cores;
    private final double boostClockGhz;
    private final int tdpWatts;
    public ProcessorUnit(String modelName, double priceUah, String socket,
                         int cores, double boostClockGhz, int tdpWatts) {
        super(modelName, priceUah);
        this.socket = socket;
        this.cores = cores;
        this.boostClockGhz = boostClockGhz;
        this.tdpWatts = tdpWatts;
    }
    public String getSocket() {
        return socket;
    }
    @Override
    public int getPowerWatts() {
        return tdpWatts;
    }
    @Override
    public double getBenchmarkScore() {
        return cores * boostClockGhz * 120.0;
    }
    @Override
    public String getCategory() {
        return "CPU";
    }
}
class GraphicsAccelerator extends AbstractHardwareComponent {
    private final int vramGb;
    private final int tensorCores;
    private final int boardPowerWatts;

    public GraphicsAccelerator(String modelName, double priceUah, int vramGb,
                               int tensorCores, int boardPowerWatts) {
        super(modelName, priceUah);
        this.vramGb = vramGb;
        this.tensorCores = tensorCores;
        this.boardPowerWatts = boardPowerWatts;
    }
    @Override
    public int getPowerWatts() {
        return boardPowerWatts;
    }
    @Override
    public double getBenchmarkScore() {
        return vramGb * 250.0 + tensorCores * 1.5;
    }
    @Override
    public String getCategory() {
        return "GPU";
    }
}
class NvmeSsdDrive extends AbstractHardwareComponent {
    private final int capacityGb;
    private final int readSpeedMbSec;
    private final int[] speedSamples;
    public NvmeSsdDrive(String modelName, double priceUah, int capacityGb,
                        int readSpeedMbSec, int[] speedSamples) {
        super(modelName, priceUah);
        this.capacityGb = capacityGb;
        this.readSpeedMbSec = readSpeedMbSec;
        this.speedSamples = speedSamples.clone();
    }
    public int[] getSpeedSamples() {
        return speedSamples.clone();
    }
    @Override
    public int getPowerWatts() {
        return 12;
    }
    @Override
    public double getBenchmarkScore() {
        return readSpeedMbSec * 0.25;
    }
    @Override
    public String getCategory() {
        return "NVMe SSD";
    }
}
class WorkstationPC {
    private final String buildName;
    private final Motherboard motherboard;
    private final PowerSupplyUnit psu;
    private final AbstractHardwareComponent[] components;
    private int componentCount;
    public WorkstationPC(String buildName, Motherboard motherboard, PowerSupplyUnit psu) {
        this.buildName = buildName;
        this.motherboard = motherboard;
        this.psu = psu;
        this.components = new AbstractHardwareComponent[motherboard.getMaxExpansionSlots()];
        this.componentCount = 0;
    }
    public boolean installComponent(AbstractHardwareComponent comp) {
        if (componentCount >= motherboard.getMaxExpansionSlots()) {
            System.out.println("Помилка: немає вільних слотів!");
            return false;
        }
        if (comp.getCategory().equals("CPU")) {
            ProcessorUnit cpu = (ProcessorUnit) comp;

            if (!motherboard.isSocketCompatible(cpu.getSocket())) {
                System.out.println("[ПЕРЕВІРКА СУМІСНОСТІ]: Відмова: Процесор " +
                        comp.getModelName() + " (" + cpu.getSocket() +
                        ") несумісний із платою " + motherboard.getModel() +
                        " (Сокет " + motherboard.getSocketType() + ")!");
                return false;
            }
        }
        int newPower = getTotalPowerWatts() + comp.getPowerWatts();
        if (newPower > psu.getSafeContinuousWatts()) {
            System.out.println("Помилка: перевищено безпечний ліміт блоку живлення!");
            return false;
        }
        components[componentCount] = comp;
        componentCount++;
        System.out.println("Встановлено: " + comp);
        return true;
    }
    public int getTotalPowerWatts() {
        int totalPower = 0;
        for (int i = 0; i < componentCount; i++) {
            totalPower = totalPower + components[i].getPowerWatts();
        }
        return totalPower;
    }
    public double getTotalSystemScore() {
        double totalScore = 0;
        for (int i = 0; i < componentCount; i++) {
            totalScore = totalScore + components[i].getBenchmarkScore();
        }
        return totalScore;
    }
    public double getTotalBuildPrice() {
        double totalPrice = 0;
        for (int i = 0; i < componentCount; i++) {
            totalPrice = totalPrice + components[i].getPriceUah();
        }
        return totalPrice;
    }
    @Override
    public String toString() {
        return "\n[ПАСПОРТ ЗБІРКИ '" + buildName + "']\n" +
                "Материнская плата: " + motherboard.getModel() +
                " (Socket: " + motherboard.getSocketType() +
                ", Слотів: " + componentCount + "/" +
                motherboard.getMaxExpansionSlots() + ")\n" +
                "Блок споживання: " + psu.getModel() +
                " (Безпечний ліміт: " + psu.getSafeContinuousWatts() +
                " Вт, КПД: " +
                (psu.getEfficiencyCoefficient() * 100) + "%)\n" +
                "Внутрішнє споживання: " + getTotalPowerWatts() +
                " Вт (З розетки: " +
                (Math.round(psu.calculateWallPowerDraw(getTotalPowerWatts()) * 100.0) / 100.0) +
                " Вт)\n" +
                "Сумарний індекс продуктивності: " +
                (Math.round(getTotalSystemScore() * 100.0) / 100.0) +
                " балів\n" +
                "Загальна вартість конфігурації: " +
                (Math.round(getTotalBuildPrice() * 100.0) / 100.0) + " грн";
    }
}
public class Task2WorkstationConfigurator {
    public static void main(String[] args) {
        System.out.println("================ КОНФИГУРАТОР РОБОЧОЇ СТАНЦІЇ ================");
        Motherboard motherboard = new Motherboard(
                "ASUS ROG B650", "AM5", 4
        );
        PowerSupplyUnit psu = new PowerSupplyUnit(
                "Seasonic 750W Gold", 750, 0.90
        );
        WorkstationPC workstation = new WorkstationPC(
                "DeepLearning-Rig", motherboard, psu
        );
        ProcessorUnit intel = new ProcessorUnit(
                "Intel Core i9", 25000, "LGA1700", 24, 6.0, 250
        );
        workstation.installComponent(intel);
        ProcessorUnit amd = new ProcessorUnit(
                "AMD Ryzen 9 7950X", 28000, "AM5", 16, 5.7, 170
        );
        GraphicsAccelerator gpu = new GraphicsAccelerator(
                "NVIDIA RTX 4080 Super", 45000, 16, 552, 320
        );
        int[] samples = {7400, 7200, 7500};
        NvmeSsdDrive ssd = new NvmeSsdDrive(
                "Samsung 990 Pro 2TB", 5900, 2000, 7450, samples
        );
        workstation.installComponent(amd);
        workstation.installComponent(gpu);
        workstation.installComponent(ssd);
        System.out.println("\nПеревірка захисного копіювання:");
        samples[0] = 100;
        int[] copiedSamples = ssd.getSpeedSamples();
        copiedSamples[1] = 100;
        System.out.println("Вихідний масив після зміни: " + samples[0]);
        System.out.println("Масив всередині SSD: " + ssd.getSpeedSamples()[0]);
        System.out.println("Масив після зміни копії: " + ssd.getSpeedSamples()[1]);
        System.out.println(workstation);
        System.out.println("==============================================================");
    }
}
