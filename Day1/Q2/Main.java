package Day1.Q2;

class PowerManager{
    private byte sectorStates;

    public void turnOnSector(int sectorIndex){
        sectorStates= (byte)(sectorStates|(1<<sectorIndex));
    }

    public void turnOffSector(int sectorIndex){
        sectorStates= (byte)(sectorStates&~(1<<sectorIndex));
    }

    public boolean isSectorOn(int sectorIndex){
        return (sectorStates&(1<<sectorIndex))!=0;
    }

    public void displaySectorStates(){
        System.out.println("Sector States: "+String.format("%8s", Integer.toBinaryString(sectorStates & 0xFF)).replace(' ', '0'));
    }
}

public class Main {
    public static void main(String[] args) {
        PowerManager manager= new PowerManager();
        manager.turnOnSector(0);
        manager.turnOnSector(3);
        manager.turnOnSector(5);

        manager.displaySectorStates();


        System.out.println("Is Sector 3 On? "+manager.isSectorOn(3));
        System.out.println("Is Sector 2 On? "+manager.isSectorOn(2));

        manager.turnOffSector(3);

        manager.displaySectorStates();

        System.out.println("Is Sector 3 On? "+manager.isSectorOn(3));
    }
}
