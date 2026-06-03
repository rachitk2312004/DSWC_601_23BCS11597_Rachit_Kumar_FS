package Day1.Q3;

class DNASequencer{
    private StringBuilder dnaSequence;

    public DNASequencer(int capacity){
        dnaSequence= new StringBuilder(capacity);
    }

    public void ingestSequence(char[] sensorData){
        for(int i=0; i<sensorData.length; i++){
            dnaSequence.append(sensorData[i]);
        }
    }

    public void mutateDNA(String target, String replacement){
        int index= dnaSequence.indexOf(target);
        
        if(index!=-1){
            dnaSequence.replace(index, index+target.length(), replacement); 
        }
    }

    public void displayDNA(){
        System.out.println("DNA Sequence: "+dnaSequence);
    }
}
public class Main {
    public static void main(String[] args){
        DNASequencer sequencer = new DNASequencer(100000);

        char[] sensorData = {'A', 'C', 'G', 'T', 'A', 'C', 'G', 'T'};

        sequencer.ingestSequence(sensorData);
        sequencer.displayDNA();

        sequencer.mutateDNA("ACG", "TTA");
        sequencer.displayDNA();
    }
}
