import java.util.ArrayList;

public class Main
{
    public static void main(String[] args) {

        // array of all jump command
        String[] brunches = {"b", "bal" , "beq","beqz", "bgez", "bgez", "bgezal" , "bgtz", "blez", "bltz", "bltzal", "bne", "bnez", "j", "jal", "jalr" , "jr" };

        //read configuration file and trace file
        ReadFiles file = new ReadFiles();

        //create and modified BTB
        BTB myBTB = new BTB(file.getState(),file.getType(),file.getImod(),file.getGshare(), file.getHistory());
        ArrayList<String> commands = file.getCommands();
        ArrayList<String> commandsIds = file.getCommandsAddresses();
        ArrayList<String> addresses = file.getAddresses();
        ArrayList<String> registers = file.getRegisters();

        int counter = 0;
        long currentAddress;

        for (int i = 0 ; i< commands.size()-1; i++)
        {
            currentAddress = Long.parseLong(addresses.get(i) , 32);
            for (String s : brunches)
            {

                if(commands.get(i).equals(s)) {
                    //sand to BTB
                    myBTB.addNewJump(commands.get(i), registers.get(i), addresses.get(i),commandsIds.get(i),  addresses.get(i+1), currentAddress);
                    counter++;
                    currentAddress = myBTB.getCurrentAddress();
                }
            }

        }
        System.out.println();
        System.out.print("hits: ");
        System.out.println(myBTB.getHit());
        System.out.print("miss: ");
        System.out.println(myBTB.getMiss());
        System.out.print("total jump commands: ");
        System.out.println(counter);
    }

}
