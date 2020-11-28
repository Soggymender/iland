package org.tiland;

import java.util.ArrayList;
import java.io.*;

public class Script {
   
    public String name;
    public String filename;

    public ArrayList<String> commands = new ArrayList<String>();
    public int numCommands = 0;
    public int nextCommand = 0;

    public boolean talking = false;

    public Script(String name) {
        this.name = name;
    
        this.filename = new String("src/main/resources/tiland/scripts/" + name);

        File file = new File(filename);

        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader(file));

            String st;
            while ((st = br.readLine()) != null) {

                addCommand(st);
            }
        } catch(Exception e) {

        }
    }

    private void addCommand(String command) {

        commands.add(command);
        numCommands++;
    }
}
