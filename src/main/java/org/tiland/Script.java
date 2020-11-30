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

                // Toss whitespaces.
                if (!st.isBlank()) {

                    st = st.replace("\t", "");
                    st = st.trim();
                    addCommand(st);
                }
            }
        } catch(Exception e) {

        }
    }

    private void addCommand(String command) {

        commands.add(command);
        numCommands++;
    }

    public void gotoLabel(String label) {

        for (int i = 0; i < numCommands; i++) {
            String command = commands.get(i);

            if (command.equals(label)) {
                nextCommand = i + 1;
                break;
            }
        }
    }
}
