import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.lang.String;

public class ReadFiles
{
    private final ArrayList<String> commands = new ArrayList<>();
    private final ArrayList<String> commandsAddresses = new ArrayList<>();
    private final ArrayList<String> addresses = new ArrayList<>();
    private final ArrayList<String> registers = new ArrayList<>();
    private String fileName;
    private String state;
    private String type;
    private String imod;
    private String gshare;
    private String history;

    public ReadFiles()
    {
        setConfiguration();
        try {
            File traceFile = new File("./RiscV traces with no register values/" + this.fileName);
            Scanner myReader = new Scanner(traceFile); //create a reader for the file
            while (myReader.hasNextLine())
            {
                // read and save addresses
                String data = myReader.nextLine();
                String[] str = data.split("," , 2);
                str = str[1].split(" ",2);
                str = str[1].split("x",2);
                str = str[1].split("\\(",2);

                addresses.add(str[0]);

                // read assembly commands addresses
                str = str[1].split(":" , 2);
                str = str[1].split(" ",2);
                str = str[1].split(" ",2);
                commandsAddresses.add(str[0]);

                // read assembly commands
                str = str[1].split(" " , 2);
                commands.add(str[0]);

                // read and save registers  and jumps values
                if (str.length <= 1)
                {
                    registers.add("");
                }
                else {
                    String regs = str[1].replaceAll("\\s+", "");
                    registers.add(regs);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    //------------------------------------------------------
    // read configuration file and set the main defualts
    //------------------------------------------------------

    private void setConfiguration()
    {
        String strconfig;
        try {
            File config = new File("./configuration.rtf");
            Scanner myReader = new Scanner(config); //create a reader for the file
            while (myReader.hasNextLine())
            {
                // read and save addresses
                String data = myReader.nextLine();
                String[] str = data.split(":", 2);
                strconfig = str[0];
                str = str[1].split("\"", 2);
                str = str[1].split("\"", 2);
                switch(strconfig)
                {
                    case "File name":
                        this.fileName = str[0];
                        break;
                    case "State machine":
                        this.state = str[0];
                        break;
                    case "predictor type":
                        this.type = str[0];
                        break;
                    case "I- mode":
                        this.imod = str[0];
                        break;
                    case "G-share":
                        this.gshare = str[0];
                        break;
                    case "History bit":
                        this.history = str[0];
                        break;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    public ArrayList<String> getCommands()
    {
        return commands;
    }

    public ArrayList<String> getAddresses() {
        return addresses;
    }

    public ArrayList<String> getCommandsAddresses() {
        return commandsAddresses;
    }

    public ArrayList<String> getRegisters() {
        return registers;
    }
    public String getState(){
        return state;
    }
    public String getType(){
        return type;
    }
    public String getImod(){
        return imod;
    }
    public String getGshare(){
        return gshare;
    }
    public String getHistory(){
        return history;
    }
}
