import java.io.*;
import java.util.*;

class Parser
{
    String commandName = "";
    String[] args;

    int length;
    //This method will divide the input into commandName and args
    public boolean parse(String input)
    {

        //if it is already one word (command only without arguments)
        if( !input.contains(" ") )
            commandName = input;

        else
        {
            //the command keyword (commandName)
            commandName = input.substring(0, input.indexOf(" "));
            input = input.substring(input.indexOf(" ") + 1, input.length());  //the args
            args = input.split(" "); //Store args in the String[]
        }

        //To get the number of args
        if(this.args != null) {length = args.length;}
        else {length = 0;}

        if( commandName.matches("echo") && length == 1 ) return true;
        else if( commandName.matches("pwd") && length == 0 ) return true;
        else if( commandName.matches("cd") && (length == 0 || length == 1) ) return true;
        else if( commandName.matches("ls") && length == 0 ) return true;
        else if( commandName.matches("ls") && args[0].equals("-r") && length == 1 ) return true;
        else if( commandName.matches("mkdir") && length != 0) return true;
        else if( commandName.matches("rmdir") && length == 1 ) return true;
        else if( commandName.matches("touch") && length == 1) return true;
        else if( commandName.matches("cp") && length == 2 ) return true;
        else if( commandName.matches("cp") && args[0].equals("-r")  && length == 3 ) return true;
        else if( commandName.matches("rm") && length == 1 ) return true;
        else if( commandName.matches("cat") && (length == 1 || length == 2) ) return true;
        else if( commandName.matches("exit") && length == 0 ) return true;
        else {
            System.out.print("Error: command not found or Invalid parameters are entered!\n");
            return false;
        }
    }

    public String getCommandName()
    {
        return commandName;
    }

    public String[] getArgs()
    {
        return args;
    }
} //end of parser class

public class Terminal
{
    boolean flag = false;
    Parser parser = new Parser();
    private static String currentDirectory = System.getProperty("user.home");
    private static String userHomeDir = System.getProperty("user.home");

    private static File file = null;

    Terminal(String input)
    {
        flag = parser.parse(input);
    }

    public void echo(String [] args)
    {
        System.out.println(args[0]);
    }

    public String pwd()
    {
       return currentDirectory;
    }

    public static void cd(String[] args)  throws IOException {
        if( args != null && args.length==1 ) {
            file = new File(args[0]);
            if( !(file.isAbsolute()) ){
                file = new File(currentDirectory + "\\" + args[0]);
            }
            if(file.exists()) {

                if(file.isDirectory()) // Checking if it's a directory and not a file
                    currentDirectory = file.getCanonicalPath(); // canonical resolves .. in paths by default

                else System.out.println("ERROR: only directories can be accessed!");
            }
            else System.out.println("ERROR: no such file or directory!");
        }
        else{
            currentDirectory = userHomeDir;
            file = new File(currentDirectory);
        }
    }

    public void ls() {
        File[] files = new File(currentDirectory).listFiles();
        Arrays.sort(files);
        for (int i=0; i < files.length; i++) {
            if(files[i].isHidden()) continue; // Don't print hidden files
            System.out.print(files[i].getName());
            if(files[i].isDirectory())
                System.out.print("\\");
            System.out.print("\n");
        }
    }

    public void lsr(String [] args){
        File[] files = new File(currentDirectory).listFiles();
        Arrays.sort(files, Collections.reverseOrder());
        for (int i=0; i < files.length; i++) {
            if(files[i].isHidden()) continue; // Don't print hidden files
            System.out.print(files[i].getName());
            if(files[i].isDirectory())
                System.out.print("\\");
            System.out.print("\n");
        }
    }

    public void mkdir(String[] args) {

        for(int i=0; i < args.length; i++)
        {
            file = new File(args[i]);
            if( !(file.isAbsolute()) )
                file = new File(currentDirectory + "\\" + args[i]);

            if(!file.exists())
                file.mkdir();

            else System.out.println("Directory already exists.");
        }

    }

    public void rmdir(String[] args) {
        if(args[0].equals("*")){
            file = new File(currentDirectory);
            File[] directoryList = file.listFiles();
            if (directoryList != null) {
                for (File child : directoryList) {
                    if(child.isDirectory())
                    {
                        if(child.list() != null && child.list().length > 0)
                            continue;
                        else
                            child.delete();
                    }
                }
            }
        }
        else{
            file = new File(args[0]);
            if( !(file.isAbsolute()) )
                file = new File(currentDirectory + "\\" + args[0]);

            if(file.exists())
            {
                if(file.isDirectory())
                {
                    if(file.list().length>0)
                        System.out.println("This directory is not empty.");
                    else
                        file.delete();
                }

            }
            else System.out.println("This directory already not exists.");
        }
    }

    public void touch(String[] args) throws IOException{
        file = new File(args[0]);
        if( !(file.isAbsolute()) )
            file = new File(currentDirectory + "\\" + args[0]);

        file.createNewFile();
    }

    public void cp(String[] args) throws IOException{
        File src = new File(args[0]);
        if (!(src.isAbsolute()))
            src = new File(currentDirectory + "\\" + args[0]);

        File dest = new File(args[1]);
        if (!(dest.isAbsolute()))
            dest = new File(currentDirectory + "\\" + args[1]);

        // remove the content of the destination file
        PrintWriter writer = new PrintWriter(dest);
        writer.print("");
        writer.close();

        FileReader fr = new FileReader(src);
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter(dest, true);
        String s;

        while ((s = br.readLine()) != null) { // read a line
            fw.write(s); // write to output file
            fw.write("\n");
            fw.flush();
        }
        br.close();
        fw.close();
    }

    public void copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory())
        {
            if (!destination.exists()) destination.mkdirs();

            String files[] = source.list();

            for (String file : files)
            {
                File sourceFile = new File(source, file);
                File destinationFile = new File(destination, file);

                copyDirectory(sourceFile, destinationFile);
            }
        }
        else
        {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(destination);

            byte[] buffer = new byte[1024];

            int length;
            while ((length = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }

    }

    public void cpr(String[] args) throws IOException{
        File src = new File(args[1]);
        if (!(src.isAbsolute()))
            src = new File(currentDirectory + "\\" + args[1]);

        File dest = new File(args[2]);
        if (!(dest.isAbsolute()))
            dest = new File(currentDirectory + "\\" + args[2]);


        copyDirectory(src, dest);
    }

    public void rm(String[] args) {
        file = new File(args[0]);

        if( !(file.isAbsolute()) )  // Resolving relative files
            file = new File(currentDirectory + "\\" + args[0]);

        if(file.exists()) // Checking if it exists
        {
            if(file.isFile()) // Checking if it's a directory and not a file
                file.delete();// Deleting the file

            else System.out.println("ERROR: only files can be deleted!");
        }
        else  System.out.println("ERROR: no such file or directory!");

    }

    public void cat(String[] args) throws IOException{
        file = new File(args[0]);
        if (!(file.isAbsolute()))
            file = new File(currentDirectory + "\\" + args[0]);

        Scanner sc = new Scanner(file);
        String str="";
        while(sc.hasNextLine()){
            str += sc.nextLine();
            str += "\n";
        }
        if(args != null && args.length==2){
            file = new File(args[1]);
            if (!(file.isAbsolute()))
                file = new File(currentDirectory + "\\" + args[1]);

            Scanner sc1 = new Scanner(file);
            String str1="";
            while(sc1.hasNextLine()){
                str1 += sc1.nextLine();
                str1 += "\n";
            }
            str += str1;
        }
        System.out.println(str);
    }

    public void exit(){
        System.exit(0);
    }

    //This method will choose the suitable command method to be called
    public void chooseCommandAction() throws IOException{
        if(flag){
            String cmdName = parser.getCommandName();
            String [] argsArray = parser.getArgs();
            if(cmdName.equals("echo")) echo(argsArray);
            else if (cmdName.equals("pwd")) System.out.println(pwd());
            else if (cmdName.equals("cd")) cd(argsArray);
            else if (cmdName.equals("ls") && argsArray==null) ls();
            else if (cmdName.equals("ls") && argsArray!=null) lsr(argsArray);
            else if(cmdName.equals("mkdir")) mkdir(argsArray);
            else if(cmdName.equals("rmdir")) rmdir(argsArray);
            else if(cmdName.equals("touch")) touch(argsArray);
            else if(cmdName.equals("cp") && argsArray.length==2) cp(argsArray);
            else if(cmdName.equals("cp") && argsArray.length==3) cpr(argsArray);
            else if(cmdName.equals("rm")) rm(argsArray);
            else if(cmdName.equals("cat")) cat(argsArray);
            else if(cmdName.equals("exit")) exit();
        }

    }


    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        while(true){
            String s = in.nextLine();
            Terminal t = new Terminal(s);
            t.chooseCommandAction();
        }
    }//end of main
}
