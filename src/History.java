import java.util.ArrayList;

public class History
{

    private int currentState = -1;
    private final ArrayList<String> stateMachines = new ArrayList<>();
    private final int historyBit;
    private  String BHR ="";

    public History (String mod, int bit)
    {
     this.historyBit = bit;
        switch (mod)
        {
            case "1":
                oneBit(true);
                break;
            case "2": {
                // set BHR to zeroes
                for(int i=0 ; i< historyBit ; i++)
                    BHR = BHR + "0";
                createStateMachines();
                twoBit(true);
                break;
            }
            case "chooser":
                for(int i=0 ; i< historyBit ; i++)
                    BHR = BHR + "0";
        }
    }

    //--------------------------------
    // create states machines 2^historyBit
    // set all as WT-10
    //-------------------------------

    private void createStateMachines()
    {
        int bounndry = (int)Math.pow(2,historyBit);

        for (int i= 0;  i < bounndry; i++)
            this.stateMachines.add("10");

    }

    //--------------------------------
    // oneBit method will check if history is set and make +/- using checker
    //-------------------------------
    public void oneBit(boolean checker)
    {
       if (currentState == -1)
           this.currentState = 1;
       else if (checker)
       {
           if (currentState == 0)
               currentState = 1;
       }
       else
       {
           if(currentState == 1)
               currentState = 0;
       }
    }

    private void twoBit(boolean flag)
    {
        if(flag)
            positiveState(Integer.parseInt(BHR));
        else
            negativeState(Integer.parseInt(BHR));
    }


    public boolean positiveState(int idx)
    {
        switch (stateMachines.get(idx))
        {
            case "11": // form ST to ST
                return true;
            case "10": //from  WT to ST
                this.stateMachines.set(idx , "11");
                return true;
            case "01": // from WNT to WT
                this.stateMachines.set(idx , "10");
                return true;
            case "00": //from SNT to WT
                this.stateMachines.set(idx , "01");
                return false;

        }

         return true;
    }

    public boolean negativeState(int idx)
    {
        switch (stateMachines.get(idx))
        {
            case "00": // form SNT to SNT
                return false;
            case "01": //from  WNT to SNT
                this.stateMachines.set(idx , "00");
                return false;
            case "10": // from WT to WNT
                this.stateMachines.set(idx , "01");
                return false;
            case "11": //from ST to WT
                this.stateMachines.set(idx , "10");
                return true;

        }

        return false;
    }
    public void setCurrentState(int num){
        currentState = num;
    }
    public String getBHR(){
        return this.BHR;
    }
    public void setBHR(String bhr){
        this.BHR = bhr;
    }
    public String  getStateMachine(int idx){
        return this.stateMachines.get(idx);
    }
}
