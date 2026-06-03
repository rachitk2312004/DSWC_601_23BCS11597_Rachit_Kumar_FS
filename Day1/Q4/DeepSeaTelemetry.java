package Day1.Q4;


//this is for checked exception
class HardwareLockException extends Exception{
    public HardwareLockException(String message){
        super(message);
    }
}

//this is for uncheked Exception

class SensorCorruptionException extends RuntimeException{
    public SensorCorruptionException(String message){
        super(message);
    }
}

//Resouorce
class TelemetryStream implements AutoCloseable{
    public void readData(){
        System.out.println("Reading data from telemetry stream...");
    }

    @Override
    public void close(){
        System.out.println("Closing telemetry stream...");
    }
}

public class DeepSeaTelemetry{
    public static void parseTelemetry(boolean fileLocked, boolean sensorCorrupted) throws HardwareLockException{
        try(TelemetryStream stream =new TelemetryStream()){
            stream.readData();

            if(fileLocked){
                throw new HardwareLockException("Hardware lock detected! Cannot access telemetry data.");
            }

            if(sensorCorrupted){
                throw new SensorCorruptionException("Sensor corruption detected! Telemetry data may be unreliable.");
            }

            System.out.println("Telemetry data parsed successfully.");
        }
    }

    public static void main(String[] args){
        try{
            parseTelemetry(true, false);
        }
        catch (HardwareLockException e){
            System.out.println("FATAL Error:" + e.getMessage());
        }
        catch (SensorCorruptionException e){
            System.out.println("WARNING:" + e.getMessage());
        }   
    }
}
