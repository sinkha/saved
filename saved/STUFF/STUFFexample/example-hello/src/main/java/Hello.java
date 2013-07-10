import java.awt.*;
import java.awt.event.*;

import com.cdmtech.atlas.example.example_hello.Version;

import gnu.getopt.Getopt;

public class Hello extends Frame {
    public static void main(String[] args) {
        String hellotext = "Hello World!";
        Getopt g = new Getopt("Example", args, "ht:");
        int c;

        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'h':
                printUsage();
                System.exit(1);

                break;

            case 't':
                hellotext = g.getOptarg();

                break;

            default:
                System.err.println("\n\nUnsupported option: "+(char) c+"\n");
                printUsage();
                System.exit(1);

                break;
            }
        }
        new Hello(hellotext);
    }

    private static void printUsage() {
        System.err.println("usage: java "+Hello.class.getName()+" (options)");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -h        This help screen.");
        System.err.println("  -t <text> Hello text to display.");
    }

    Hello(String text) {
    	
    	Label hello;
    	try {
    		Version version = new Version();
        	hello = new Label(text + " The build version is " + version.toString() + " " 
        			+ version.getDateTimeStampString() + "\nSCM number: " + version.getSCMBuildNumber());

    	} catch(Exception e) {
    		hello = new Label(text);
    	}
        add(hello, "Center");

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we){
                    dispose();
                    System.exit(0);
                }
            });

        setSize(200, 200);
        setVisible(true);
    }
}
