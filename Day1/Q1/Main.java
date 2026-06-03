package Day1;

//constraints are that it should have a ship id which will be <=30000
//baynumber(max 100), operational status("active" or "not active"), fleet classification(A,b,c)
//Oreweight
//Total Fleet Values(can exceed 40 billion)
abstract class SpaceVessel{
    protected short shipId;
    protected boolean operationalStatus;
    protected char fleetClassification;

    public SpaceVessel(short shipId, boolean operationalStatus, char fleetClassification) {
        this.shipId = shipId;
        this.operationalStatus = operationalStatus;
        this.fleetClassification = fleetClassification;
    }
}

//mining ship has ore weights in 2d array, the rows are bays and the columns are containers
class MiningShip extends SpaceVessel{
    private float[][] oreWeights;

    public MiningShip(short shipId, boolean operationalStatus, char fleetClassification, float[][] oreWeights) {
        super(shipId, operationalStatus, fleetClassification);
        this.oreWeights = oreWeights;
    }

    public float getTotalWeight(){
        float weight=0;

        for(int i=0;i<oreWeights.length;i++){
            for(int j=0;j<oreWeights[i].length;j++){
                weight+=oreWeights[i][j];
            }
        }

        return weight;
    }

    public float findHeaviestConatainer(){
        float maxWeight=0;

        for(int i=0;i<oreWeights.length;i++){
            for(int j=0;j<oreWeights[i].length;j++){
                if(oreWeights[i][j]>maxWeight){
                    maxWeight=oreWeights[i][j];
                }
            }
        }

        return maxWeight;
    }
}

class Main{
    public static void main(String[] args){

        float[][] oreWeights = {{10.5f, 20.3f, 15.2f}, {5.0f, 12.8f, 8.6f}};

        SpaceVessel[] fleet = new SpaceVessel[2];

        fleet[0]= new MiningShip((short) 12345, true, 'A', oreWeights);

        MiningShip miningShip = (MiningShip) fleet[0];

        System.out.println("Total Ore Weight: " + miningShip.getTotalWeight());
        System.out.println("Heaviest Container Weight: " + miningShip.findHeaviestConatainer());
    }
}