import java.awt.*;
import java.awt.event.*;

import gnu.getopt.Getopt;

public class Goodbye extends Frame {
    public static void main(String[] args) {
        String byetext = "Goodbye, Cruel World!";
        Getopt g = new Getopt("Example", args, "ht:");
        int c;

        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'h':
                printUsage();
                System.exit(1);

                break;

            case 't':
                byetext = g.getOptarg();

                break;

            default:
                System.err.println("\n\nUnsupported option: "+(char) c+"\n");
                printUsage();
                System.exit(1);

                break;
            }
        }
        new Goodbye(byetext);
    }

    private static void printUsage() {
        System.err.println("usage: java "+Goodbye.class.getName()+" (options)");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -h        This help screen.");
        System.err.println("  -t <text> Goodbye text to display.");
    }

    Goodbye(String text) {
    	
    	Label bye = new Label(text);

        add(bye, "Center");

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
