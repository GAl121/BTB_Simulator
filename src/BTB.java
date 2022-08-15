import java.util.ArrayList;

public class BTB
{
    private final ArrayList<String> brunchId = new ArrayList<>();
    private final ArrayList<Long> targetPc = new ArrayList<>();
    private final ArrayList<History> history = new ArrayList<>();
    private String bitMod;
    private long currentAddress;
    private long nextAddress;
    private int hit = 0;
    private int miss = 0;
    private boolean global;
    private boolean local;
    private boolean iselect;
    private boolean ishare;
    private boolean gshare; // true - on , false - off
    private int historyBit;
    private String address;
    private String chooser = "10";

    //-------------------------------------------------
    //constructor BTB
    //-------------------------------------------------

    public BTB(String state, String type, String imod, String gshare , String history)
    {
        setBTBConfiguration(state, type,imod, gshare, history);

    }

    //----------------------------------------------------
    //store and create new jump
    //----------------------------------------------------

    public void addNewJump(String command, String registers, String address ,String commandId,  String nextAddress, long myAddress )
    {
        this.address = address;
        this.currentAddress = myAddress;
        this.nextAddress = convertAddress(nextAddress);
        switch (bitMod)
        {
            case "1":
                oneBit(command,registers);
                break;
            case "2":
                twoBit(command , commandId,registers);
                break;
        }
    }

//=======================================================================
// start block states machine
//=======================================================================

    //----------------------------------------------------
    //convert address from hex to long
    //----------------------------------------------------

    private long convertAddress(String reg)
    {
        String str;

        if(reg.equals("j"))
           return Long.parseLong(reg,32);
        else  if(reg.contains(",")) {
            int idx = reg.lastIndexOf(",");
            str = reg.substring(idx + 1);
            return Long.parseLong(str, 32);
        }
        else
            return Long.parseLong(reg,32);
    }
    //----------------------------------------------------
    // oneBit state machine return history
    //----------------------------------------------------

    private void oneBit(String command, String reg)
    {
        int idx = checkExistJump(command);
        // if  brunch doesn't exist in BTB we save as hit
        if( idx == -1) {
            this.brunchId.add(command);
            this.targetPc.add(convertAddress(reg));
            this.history.add(new History(bitMod, historyBit));
           hit++;
        }
        else
        {
            if(convertAddress(reg) == targetPc.get(idx)) {
                history.get(idx).oneBit(true);
                if(nextAddress == targetPc.get(idx))
                    hit++;
                else {
                    history.get(idx).oneBit(false);
                    miss++;
                }
                currentAddress = targetPc.get(idx);
            }
            else {
                history.get(idx).oneBit(false);
                currentAddress += 4;
                miss++;
            }

        }
    }

    //----------------------------------------------------
    // twoBit state machine return history
    //----------------------------------------------------

    private void twoBit(String command, String commandId, String reg)
    {
        //for tournament predictor
        if(this.local && this.global)
            tournamentPredictor(command,commandId,reg);
        // for local predictor
        else if (this.local && !this.global)
           localPredictor(commandId, reg);
        else{
            //for global predictor
            if(!this.local && this.global)
               globalPredictor(command, reg);
            else
                System.out.println("predictor must configure as local/global/tournament");
        }

    }


    //----------------------------------------------------
    // tournament predictor method
    // local - 10, 11
    //global - 00 , 01
    //----------------------------------------------------

    private void tournamentPredictor(String command, String commandId ,  String reg)
    {
        int idx;
        boolean local = localPredictor(commandId,reg);
        boolean global = globalPredictor(command,reg);

        if(chooser.equals("11") || chooser.equals("10"))
        {
            // take local , delete global
            idx = checkExistJump(command);
            deleteOne(idx);
            //check both of predictor so delete hits/miss from global predictor
            if(global)
                hit--;
            else
                miss--;
        }
        else if(chooser.equals("00") || chooser.equals("01"))
        {
            // take global , delete local
            idx = checkExistJump(commandId);
            deleteOne(idx);
            //check both of predictor so delete hits/miss from local predictor
            if(local)
                hit--;
            else
                miss--;
        }
        if (local && !global)
            setChooser(true);
        else if(!local && global)
            setChooser(false);

    }

    //-------------------------------------------------
    // method delete last command as the chooser choose
    //-------------------------------------------------
    private void deleteOne(int idx )
    {
        this.brunchId.remove(idx);
        this.targetPc.remove(idx);
        this.history.remove(idx);
    }

    //---------------------------------------------------
    // set chooser state
    // state = true , chooser +1
    // state = false, chooser -1
    //---------------------------------------------------
    private void setChooser(boolean state)
    {

        if (state) {
            //local was right, chooser +1
            switch (this.chooser) {
                case "11": // form ST to ST
                    break;
                case "10": //from  WT to ST
                    this.chooser = "11";
                    break;
                case "01": // from WNT to WT
                    this.chooser = "10";
                    break;
                case "00": //from SNT to WT
                    this.chooser = "01";
                    break;
            }
        }
        else {
            //global was right, chooser -1
            switch (this.chooser)
            {
                case "00": // form SNT to SNT
                    break;
                case "01": //from  WNT to SNT
                    this.chooser = "00";
                    break;
                case "10": // from WT to WNT
                    this.chooser = "01";
                    break;
                case "11": //from ST to WT
                    this.chooser = "10";
                    break;

            }
        }
    }

    //----------------------------------------------------
    // local predictor method
    //----------------------------------------------------
    private boolean globalPredictor(String command, String reg)
    {
        int idx = checkExistJump(command);
        int GHR;
        boolean flag = true;
        String newBHR;
        String state;

        if(idx == -1) {
            this.brunchId.add(command);
            this.targetPc.add(convertAddress(reg));
            this.history.add(new History(bitMod, historyBit));
                hit++;
        }
        else
        {
            GHR = Integer.parseInt(history.get(idx).getBHR() , 2);
            state = history.get(idx).getStateMachine(GHR);
            //BTB right and jump ,make hit
            if(targetPc.get(idx) == nextAddress && (state.equals("10") || state.equals("11")))
            {
                flag = gmodSetFlag(idx,GHR,true);
                newBHR = setBHR(history.get(idx).getBHR() , flag);
                history.get(idx).setBHR(newBHR);
                hit++;

            }

            //BTB mistake on jump, and miss
            else if(targetPc.get(idx) != nextAddress && (state.equals("10") || state.equals("11")))
            {
                flag = gmodSetFlag(idx,GHR,false);
                newBHR = setBHR(history.get(idx).getBHR() , flag);
                history.get(idx).setBHR(newBHR);
                miss++;
            }
            else {
                // BTB not jump but spouse to, make miss
                if (targetPc.get(idx) == nextAddress && (state.equals("01") || state.equals("00"))) {
                    flag = gmodSetFlag(idx, GHR, true);
                    newBHR = setBHR(history.get(idx).getBHR(), flag);
                    history.get(idx).setBHR(newBHR);
                    miss++;
                }
                //BTB not jump and not spouse to make hit
                else if (targetPc.get(idx) != nextAddress && (state.equals("01") || state.equals("00"))) {
                    flag = gmodSetFlag(idx, GHR, false);
                    newBHR = setBHR(history.get(idx).getBHR(), flag);
                    history.get(idx).setBHR(newBHR);
                    hit++;
                }
            }
        }
        return flag;
    }

    //---------------------------------------------------------------
    // set flag for gshare or none
    //---------------------------------------------------------------

    private boolean gmodSetFlag(int idx, int GHR , boolean pos)
    {
        this.address = Integer.toBinaryString(Integer.parseInt(this.address,16));
        int length = this.address.length();
        this.address = this.address.substring(this.address.length() - historyBit, length); // h bit of address

        String num;
        boolean flag;
        if (this.gshare && pos)
        {
            num = String.valueOf(GHR ^ Integer.parseInt(this.address,2));
            flag = history.get(idx).positiveState((int)(Integer.parseInt(num)%Math.pow(2,historyBit)));
        }
        else if(this.gshare && !pos) {
            num = String.valueOf(GHR ^ Integer.parseInt(this.address,2));
            flag = history.get(idx).negativeState((int)(Integer.parseInt(num)%Math.pow(2,historyBit)));
        }
        else if (!this.gshare && pos)
        {
            flag = history.get(idx).positiveState(GHR);
        }
        else {
            flag = history.get(idx).negativeState(GHR);
        }

        return flag;
    }
    //----------------------------------------------------
    // local predictor method
    //----------------------------------------------------
    private boolean localPredictor(String commandId, String reg)
    {
        int idx = checkExistJump(commandId);
        if(idx == -1) {
            this.brunchId.add(commandId);
            this.targetPc.add(convertAddress(reg));
            this.history.add(new History(bitMod, historyBit));
            hit++;
        }
        else
            return imod(commandId);
        return true;
    }

    //-----------------------------------------------
    //set new value for bhr reg
    //-----------------------------------------------

    private boolean imod(String commandId)
    {
        boolean flag = false;
        String newBHR;
        int idx = checkExistJump(commandId);
        int BHR = Integer.parseInt(history.get(idx).getBHR() , 2);

        String state = history.get(idx).getStateMachine(BHR);

            //BTB right and jump ,make hit
            if(targetPc.get(idx) == nextAddress && (state.equals("10") || state.equals("11")))
            {
                flag = imodSetFlag(idx,BHR,true);
                newBHR = setBHR(history.get(idx).getBHR() , flag);
                history.get(idx).setBHR(newBHR);
                hit++;

            }

            //BTB mistake on jump, and miss
            else if(targetPc.get(idx) != nextAddress && (state.equals("10") || state.equals("11")))
            {
                flag = imodSetFlag(idx,BHR,false);
                newBHR = setBHR(history.get(idx).getBHR() , flag);
                history.get(idx).setBHR(newBHR);
                miss++;
            }
            else {
                // BTB not jump but spouse to make miss
                if (targetPc.get(idx) == nextAddress && (state.equals("01") || state.equals("00"))) {
                    flag = imodSetFlag(idx, BHR, true);
                    newBHR = setBHR(history.get(idx).getBHR(), flag);
                    history.get(idx).setBHR(newBHR);
                    miss++;
                }
                //BTB not jump and not spouse to make hit
                else if (targetPc.get(idx) != nextAddress && (state.equals("01") || state.equals("00"))) {
                    flag = imodSetFlag(idx, BHR, false);
                    newBHR = setBHR(history.get(idx).getBHR(), flag);
                    history.get(idx).setBHR(newBHR);
                    hit++;
                }
            }
            return flag;
    }

    //---------------------------------------------------------------
    // set flag for ishare / iselect
    //---------------------------------------------------------------

    private boolean imodSetFlag(int idx, int BHR , boolean pos)
    {
        this.address = Integer.toBinaryString(Integer.parseInt(this.address,16));
        int length = this.address.length();
        this.address = this.address.substring(this.address.length() - historyBit, length); // h bit of address

        String num;
        boolean flag;
        if (iselect && pos)
        {
            num = String.valueOf(BHR + Integer.parseInt(this.address,2));

            flag = history.get(idx).positiveState((int)(Integer.parseInt(num)%Math.pow(2,historyBit)));
        }
        else if(iselect && !pos) {
            num = String.valueOf(BHR + Integer.parseInt(this.address,2));
            flag = history.get(idx).negativeState((int)(Integer.parseInt(num)%Math.pow(2,historyBit)));
        }
        else if (ishare && pos)
        {
            num = String.valueOf(BHR ^ Integer.parseInt(this.address,2));
            flag = history.get(idx).positiveState((int)(Integer.parseInt(num)%Math.pow(2,historyBit)));
        }
        else if(ishare && !pos) {
            num = String.valueOf(BHR ^ Integer.parseInt(this.address,2));
            flag = history.get(idx).negativeState((int)(Integer.parseInt(num)%Math.pow(2,historyBit)));
        }
        else if(!ishare && !iselect && pos)
            flag = history.get(idx).positiveState(BHR);
        else
            flag = history.get(idx).negativeState(BHR);
        return flag;
    }

    //-----------------------------------------------
    //set new value for bhr reg
    //-----------------------------------------------

    private String setBHR(String BHR, boolean flag)
    {
        String newBHR = BHR;
        if (flag)
        {
            newBHR += 1;
            newBHR = newBHR.substring(1,newBHR.length());
        }
        else
        {
            newBHR += 1;
            newBHR = newBHR.substring(1,newBHR.length());
        }
        return newBHR;
    }

//=======================================================================
// end block states machine
//=======================================================================

    //----------------------------------------------------
    //method check if this jump already exist in BTB
    //----------------------------------------------------

    private int checkExistJump(String jumpId)
    {
        if  (brunchId.size() == 0)
            return -1;
        for( int i = 0; i <= brunchId.size()-1 ; i++) {
            if (brunchId.get(i).contains(jumpId))
                return i;
        }
        return -1;
    }

    //-----------------------------------------------------
    // set BTB configuration
    //-----------------------------------------------------
    private void setBTBConfiguration(String state, String type, String imod, String gshare, String  history)
    {
        // set bit mode
        if(Integer.parseInt(state) >= 1 && Integer.parseInt(state) <= 3)
            this.bitMod = state;
        else {
            System.out.println("data error please check the \"State machine\" field");
            return;
        }

        // configure the type of the BTB local/global/tournament

        if(type.toLowerCase().equals("tournament"))
        {
            this.local = true;
            this.global = true;
        }
        else if (type.toLowerCase().equals("local") || type.toLowerCase().equals("global")){
            this.local = type.toLowerCase().equals("local");
            this.global = type.toLowerCase().equals("global");
        }
        else {
            System.out.println("data error please check the \"predictor type\" field");
            return;
        }


        // configure of  i mod: select/share/none
        if (imod.toLowerCase().equals("none"))
        {
            this.iselect = false;
            this.ishare = false;
        }
        else
        {
            if (imod.toLowerCase().equals("select"))
                this.iselect = true;
            else if (imod.toLowerCase().equals("share"))
                this.ishare = true;
            else
                System.out.println("data error please check the \"i-mod\" field");
        }


        // configure of g-share (true) or i-share (false)
        if(gshare.toLowerCase().equals("off"))
            this.gshare = false;
        else if (gshare.toLowerCase().equals("on"))
            this.gshare = true;
        else {
            System.out.println("data error please check the \"G-share\" field");
            return;
        }

        // configure of n-bit history
        if(Integer.parseInt(history) <= 0)
            System.out.println("data error please check the \"History bit\" field");
        else
            this.historyBit = Integer.parseInt(history);

    }

    public int getHit(){
        return this.hit;
    }
    public int getMiss(){
        return this.miss;
    }
    public long getCurrentAddress(){
        return this.currentAddress;
    }

}

