/*
 *  Copyright (c) May 11, 2020 by James D. Gay
 */
package Rje3780;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 *
 * @author james This is where all the action takes place.
 */
public class MainLoop extends Thread {

    //
    //  Ascii to EBCDIC conversion table
    //
    static char ascii_to_ebcdic[] = {
        '\0', '\001', '\002', '\003', '7', '-', '.', '/', '\026', '\005',
        '%', '\013', '\f', '\r', '\016', '\017', '\020', '\021', '\022', '\023',
        '<', '=', '2', '&', '\030', '\031', '\032', '\'', '"', '\035',
        '5', '\037', '@', 'Z', '\177', '{', '[', 'l', 'P', '}',
        'M', ']', '\\', 'N', 'k', '`', 'K', 'a', '\360', '\361',
        '\362', '\363', '\364', '\365', '\366', '\367', '\370', '\371', 'z', '^',
        'L', '~', 'n', 'o', '|', '\301', '\302', '\303', '\304', '\305',
        '\306', '\307', '\310', '\311', '\321', '\322', '\323', '\324', '\325', '\326',
        '\327', '\330', '\331', '\342', '\343', '\344', '\345', '\346', '\347', '\350',
        '\351', '\255', '\340', '\275', '_', 'm', 'y', '\201', '\202', '\203',
        '\204', '\205', '\206', '\207', '\210', '\211', '\221', '\222', '\223', '\224',
        '\225', '\226', '\227', '\230', '\231', '\242', '\243', '\244', '\245', '\246',
        '\247', '\250', '\251', '\300', 'j', '\320', '\241', '\007', 'h', '\334',
        'Q', 'B', 'C', 'D', 'G', 'H', 'R', 'S', 'T', 'W',
        'V', 'X', 'c', 'g', 'q', '\234', '\236', '\313', '\314', '\315',
        '\333', '\335', '\337', '\354', '\374', '\260', '\261', '\262', '\263', '\264',
        'E', 'U', '\316', '\336', 'I', 'i', '\004', '\006', '\253', '\b',
        '\272', '\270', '\267', '\252', '\212', '\213', '\t', '\n', '\024', '\273',
        '\025', '\265', '\266', '\027', '\033', '\271', '\034', '\036', '\274', ' ',
        '\276', '\277', '!', '#', '$', '(', ')', '*', '+', ',',
        '0', '1', '\312', '3', '4', '6', '8', '\317', '9', ':',
        ';', '>', 'A', 'F', 'J', 'O', 'Y', 'b', '\332', 'd',
        'e', 'f', 'p', 'r', 's', '\341', 't', 'u', 'v', 'w',
        'x', '\200', '\214', '\215', '\216', '\353', '\217', '\355', '\356', '\357',
        '\220', '\232', '\233', '\235', '\237', '\240', '\254', '\256', '\257', '\375',
        '\376', '\373', '?', '\352', '\372', '\377'
    };

    //
    //  EBCDIC to ascii conversion table
    //
    static char ebcdic_to_ascii[] = {
        '\0', '\001', '\002', '\003', '\246', '\t', '\247', '\177', '\251', '\260',
        '\261', '\013', '\f', '\r', '\016', '\017', '\020', '\021', '\022', '\023',
        '\262', '\n', '\b', '\267', '\030', '\031', '\032', '\270', '\272', '\035',
        '\273', '\037', '\275', '\300', '\034', '\301', '\302', '\n', '\027', '\033',
        '\303', '\304', '\305', '\306', '\307', '\005', '\006', '\007', '\310', '\311',
        '\026', '\313', '\314', '\036', '\315', '\004', '\316', '\320', '\321', '\322',
        '\024', '\025', '\323', '\374', ' ', '\324', '\203', '\204', '\205', '\240',
        '\325', '\206', '\207', '\244', '\326', '.', '<', '(', '+', '\327',
        '&', '\202', '\210', '\211', '\212', '\241', '\214', '\213', '\215', '\330',
        '!', '$', '*', ')', ';', '^', '-', '/', '\331', '\216',
        '\333', '\334', '\335', '\217', '\200', '\245', '|', ',', '%', '_',
        '>', '?', '\336', '\220', '\337', '\340', '\342', '\343', '\344', '\345',
        '\346', '`', ':', '#', '@', '\'', '=', '"', '\347', 'a',
        'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', '\256', '\257',
        '\350', '\351', '\352', '\354', '\360', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', '\361', '\362', '\221', '\363', '\222', '\364',
        '\365', '~', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '\255', '\250', '\366', '[', '\367', '\370', '\233', '\234', '\235', '\236',
        '\237', '\265', '\266', '\254', '\253', '\271', '\252', '\263', '\274', ']',
        '\276', '\277', '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
        'H', 'I', '\312', '\223', '\224', '\225', '\242', '\317', '}', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', '\332', '\226',
        '\201', '\227', '\243', '\230', '\\', '\341', 'S', 'T', 'U', 'V',
        'W', 'X', 'Y', 'Z', '\375', '\353', '\231', '\355', '\356', '\357',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '\376', '\373', '\232', '\371', '\372', '\377'
    };

    //
    //  Userful BISYNC/RJE EBCDIC characters
    //
    static final byte SOH = 0x01, STX = 0x02, ETX = 0x03, DLE = 0x10, DC1 = 0x11, DC2 = 0x12, DC3 = 0x13, DC4 = 0x14, IRS = 0x1e;
    static final byte ETB = 0x26, ESC = 0x27, ENQ = 0x2d, SYN = 0x32, EOT = 0x37, NAK = 0x3D, SPC = 0x40, ACK1 = 0x61, ACK0 = 0x70;

    //
    //  Files and socktets
    //
    static DataInputStream input;
    static BufferedReader jclReader;
    static DataOutputStream output;
    static BufferedWriter printPunch;
    static Socket sock;
    static boolean connected = false;
    static Session session = null;
    static String jclFile = "";
    public static String commandLine = "";
    static String punchFileName = "punch";
    static byte backChr;

    //
    //  Our counters
    //
    static int bytesSent = 0, bytesReceived = 0, cardsRead = 0, cardsPunched = 0, linesPrinted = 0;

    //
    //  Possible States of RJE
    //
    public enum Stat {

        Nolink, InitiaWait, Idle, Sending, SendPending, SendingCommand1, SendingCommand2, Receiving, Shutdown1, Shutdown2;
    };

    //
    //  State of RJE
    //
    public static boolean mainLoopActive = false;
    public static Stat status = Stat.Nolink;

    //
    //  Current output device
    //
    public enum OutDevice {

        console, printer, punch
    };

    //
    //  Current ACK value (bounces between ACK0 and ACK1)
    //
    static public byte ack = ACK0;

    //
    //  Values for our MVS host and other useful item
    //
    public static String hostName = "localhost";
    static String hostAddress = "";
    public static int hostPort = 37801;
    public static String signon = "RMT1";
    public static String password = "";
    public static String pemFile = "";
    public static String defaultDirectory = "";
    public static String defaultPrintDirectory = "";
    public static String defaultPunchDirectory = "";
    static String signonString = "/*SIGNON       ";
    static String signoffString = "SIGNOFF  ";
    static String fileName = "";
    static boolean traceOn = true;
    static boolean removeExtraPrinterBlankLines = true;
    static boolean removeExtraPunchCards = true;
    static boolean createSSHTunnel = false;
    static boolean punchWindow = false;
    static boolean printWindow = false;
    static Session sshSession = null;
    static String sshUsername = "";
    static String sshPassword = "";
    static JFrame punchFrame = null;
    static JTextArea punchTextArea = null;
    static boolean clearPunchTextArea = false;
    static JFrame printFrame = null;
    static JTextArea printTextArea = null;
    static boolean clearPrintTextArea = false;
    boolean addFFtoEnd = false;
    public static boolean exit = false;

    /**
     *
     */
    @Override
    public void run() {
        OutDevice outdev = OutDevice.console; //  Set output device to be console
        byte[] buffer = new byte[1000];         //  buffer for reading/writing
        int bufferPointer = 0;                  //  Pointer into buffer
        int printLines = 0;                     //  Number of lines printed
        int punchLines = 0;                     //  Number of cards punched
        int readerLines = 0;                    //  Number of cards read
        boolean firstPrint = false;             //  True if this is the very first line printed
        boolean firstPunch = false;             //  True if thie is the very first card punched
        boolean ignorePrint = false;            //  True if we treat line as console output instead of printer
        boolean searchStart = true;             //  True if we are looking for the Start Job line in a printout
        String firstLines[] = new String[100];  //  Holds 1st print lines until jobs name is seen
        int firstLineIndex = 0;                 //  Number of 1st print lines
        try {
            //
            //  Main loop, processes information coming from MVS system
            //
            while (true) {
                //
                //  Get a character from MVS (suck up 0x00 characters)
                //
                byte chr = getFromHost();
                if (chr == 0x00) {
                    traceOut(" null seen");
                    return;
                }
                //
                //  Put out character if we are tracing
                //
                if (traceOn) {
                    traceOut("<---" + decodeBisync(chr) + " Mode=" + status + " outdev=" + outdev);
                }
                //
                //  If we are in Initial Wait and the character received is ACK0 then signon
                //
                if (status == Stat.InitiaWait && chr == ACK0) {
                    //
                    //  Send signon
                    //
                    updateStatus("RJE001I - Communication started, Host: " + hostName + " IP Address: " + hostAddress);
                    if (createSSHTunnel) {
                        updateStatus("RJE002I - Link established to Host: " + hostName + " on Port: " + hostPort + " via SSH Tunnel");
                    } else {
                        updateStatus("RJE002I - Link established to Host: " + hostName + " on Port: " + hostPort);
                    }
                    byte[] loginS = {SYN, SYN, STX};
                    //
                    //  Create signon string, add password if provided
                    //
                    String SignonString = signonString + signon;
                    if ("".equals(password)) {
                        SignonString += " ";
                    } else {
                        SignonString += "," + password + " ";
                    }
                    if (traceOn) {
                        traceOut("--->SYN SYN STX " + SignonString);
                    }
                    byte[] login = createCommand(loginS, SignonString, ETX, true);
                    output.write(login);
                    bytesSent += login.length;
                    output.flush();
                }

                //
                //  If we are in Shutdown and the character received is EOT then we have signed off
                //
                if (status == Stat.Shutdown2 && chr == EOT) {
                    updateStatus("RJE999I - Signed off from host");
                    input.close();
                    output.close();
                    sock.close();
                    if (createSSHTunnel) {
                        if (sshSession != null) {
                            sshSession.disconnect();
                            sshSession = null;
                        }
                    }
                    status = Stat.Nolink;
                    updateState("NoLink");
                    connected = false;
                    mainLoopActive = false;

                    //
                    //  If we are exiting then save our parameters and exit
                    //
                    if (exit) {
                        System.exit(0);
                    }
                    return;
                }

                //
                //  If we are in initial wait and the character received is ACK1 then we have successfully signed on.  Show that we are ready to receive
                //
                if (status == Stat.InitiaWait && chr == ACK1) {
                    //
                    //  Finish signon
                    //
                    updateStatus("RJE003I - Signed on to host as " + signon);
                    byte[] eot = {SYN, SYN, EOT};
                    if (traceOn) {
                        traceOut("--->SYN SYN EOT");
                    }
                    output.write(eot);
                    bytesSent += eot.length;
                    output.flush();
                    status = Stat.Idle;
                    updateState("Idle");
                }

                //
                //  If SendingCommand1 and we received an ACK send the command
                //
                if ((status == Stat.SendingCommand1 || status == Stat.Shutdown1) && (chr == ACK0 || chr == ACK1)) {
                    byte[] commandStart = {SYN, SYN, STX};
                    if (traceOn) {
                        traceOut("--->SYN SYN STX /*" + commandLine + " ETX");
                    }
                    byte[] command = createCommand(commandStart, "/*" + commandLine, ETX, true);
                    output.write(command);
                    bytesSent += command.length;
                    output.flush();
                    if (status == Stat.SendingCommand1) {
                        status = Stat.SendingCommand2;
                    } else {
                        status = Stat.Shutdown2;
                    }
                    chr = 0x00;
                    updateStatus("--->" + commandLine);
                }

                //
                //  If SendingCommand2 or Shutdown2 and we received an ACK send the command
                //
                if ((status == Stat.SendingCommand2 || status == Stat.Shutdown2) && (chr == ACK0 || chr == ACK1)) {
                    byte[] commandEnd = {SYN, SYN, EOT};
                    if (traceOn) {
                        traceOut("--->SYN SYN EOT");
                    }
                    output.write(commandEnd);
                    bytesSent += commandEnd.length;
                    output.flush();
                    Rje.commandField.setText("");
                    if (status == Stat.Shutdown2) {
                        java.awt.EventQueue.invokeLater(() -> {
                            updateStatus("RJE998I - Shutting Down");
                            updateState("Shutting Down");
                            Rje.systemField.setText("");
                            Rje.statusField.setText("Logged Off");

                        });

                    } else {
                        status = Stat.Idle;
                    }
                }
                //
                //  If SendPending is true and we received an ACK0 then open up the card reader and show that we are sending
                //
                if (status == Stat.SendPending && chr == ACK0) {
                    status = Stat.Sending;
                    jclReader = new BufferedReader(new FileReader(jclFile));
                    readerLines = 0;
                }

                //
                //  If we are idle and we received an ENQ then reset ACK, send an ACK and then show that we are receiving
                //
                if (status == Stat.Idle && chr == ENQ) {
                    //
                    //  They want to send us something
                    //
                    ack = ACK0;
                    sendAck();
                    //updateStatus("Entering receive mode");
                    if (traceOn) {
                        traceOut("ENQ Seen, Entering receive mode");
                    }
                    status = Stat.Receiving;
                    updateState("Receiving");
                    bufferPointer = 0;
                }

                //
                //  If we are sending and we receive an ACK then send next card (This may need work in case of incorrect ack being received)
                //
                if (status == Stat.Sending) {
                    if (chr == ACK0 || chr == ACK1) {
                        String line = jclReader.readLine();
                        //
                        //  If card is null then we have reached end of the deck.  Close the reader and tell MVS we are done
                        //
                        if (line == null) {
                            jclReader.close();
                            byte[] done = {SYN, SYN, EOT};
                            if (traceOn) {
                                traceOut("--->SYN SYN EOT");
                            }
                            output.write(done);
                            bytesSent += done.length;
                            output.flush();
                            status = Stat.Idle;
                            updateState("Idle");
                            updateStatus("RJE006I - Job submitted, number of cards = " + NumberFormat.getNumberInstance(Locale.US).format(readerLines));

                        } else {
                            //
                            //  Send the card to the other side (Make sure we send exactly 80 characters filling with blanks if necessary or truncating if needed
                            //
                            if (line.length() > 80) {
                                line = line.substring(0, 79);
                            }
                            byte[] prefix = {STX};
                            byte[] packet = createCommand(prefix, line, ETX, true);
                            if (traceOn) {
                                traceOut("--->STX " + line + " ETX");
                            }
                            output.write(packet, 0, 82);
                            bytesSent += 82;
                            cardsRead++;
                            output.flush();
                            readerLines++;
                        }
                    }

                }

                //
                //  If we are idle and we receive a STX get set for possible select printer or punch command
                //
                if (status == Stat.Idle) {
                    if (chr == STX) {
                        backChr = STX;
                        //
                        //  If STX was seen and this is a DC1 then select printer
                        //
                    } else if (backChr == STX && chr == DC1 && outdev != OutDevice.printer) {
                        outdev = OutDevice.printer;
                        updateState("Receiving Printer");
                        if (traceOn) {
                            traceOut("Receiving Printer");
                        }
                        firstPrint = true;
                        ignorePrint = false;
                        printLines = 0;

                        //
                        //  If STX was seen and this is a DC2 then select punch
                        //
                    } else if (backChr == STX && chr == DC2 && outdev != OutDevice.punch) {
                        outdev = OutDevice.punch;
                        updateStatus("RJE007S - Receiving punch data");
                        if (traceOn) {
                            traceOut("Receving punch data");
                        }
                        firstPunch = true;
                        updateState("Receiving Punch");
                        punchLines = 0;

                        //
                        //  Get current date/time to append to punch file name and open punch file
                        //
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(Date.from(Instant.now()));
                        String punchName = MainLoop.punchFileName + new SimpleDateFormat("-yyyy-MM-dd-hh-mm-ss'.pch'").format(new Date());
                        if (punchFrame == null) {
                            updateStatus("RJE007T - Punch fileName=" + defaultPunchDirectory + punchName);
                        }
                        printPunch = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(defaultPunchDirectory + punchName)));
                    }
                }

                //
                //  If we are receiving process character
                //
                if (status == Stat.Receiving) {
                    //
                    //  If STX then remember for later
                    //
                    if (chr == STX) {
                        bufferPointer = 0;
                        //
                        //  If STX was seen and this is a DC1 then select printer
                        //
                    } else if (backChr == STX && chr == DC1 && outdev != OutDevice.printer) {
                        outdev = OutDevice.printer;
                        updateState("Receiving Printer");
                        if (traceOn) {
                            traceOut("Receiving Printer");
                        }
                        firstPrint = true;
                        ignorePrint = false;
                        printLines = 0;
                        if (printFrame != null && clearPrintTextArea) {
                            printTextArea.setText("");
                        }

                        //
                        //  If STX was seen and this is a DC2 then select punch
                        //
                    } else if (backChr == STX && chr == DC2 && outdev != OutDevice.punch) {
                        outdev = OutDevice.punch;
                        updateStatus("RJE007S - Receiving punch data");
                        if (traceOn) {
                            traceOut("Receving punch data");
                        }
                        updateState("Receiving Punch");
                        ignorePrint = true;
                        firstPunch = true;
                        punchLines = 0;
                        if (punchFrame != null && clearPunchTextArea) {
                            punchTextArea.setText("");
                        }

                        //
                        //  Get current date/time to append to punch file name and open punch file
                        //
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(Date.from(Instant.now()));
                        String punchName = MainLoop.punchFileName + new SimpleDateFormat("-yyyy-MM-dd-hh-mm-ss'.pch'").format(new Date());
                        if (punchFrame == null) {
                            updateStatus("RJE007T - Punch fileName=" + defaultPunchDirectory + punchName);
                        }
                        printPunch = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(defaultPunchDirectory + punchName)));

                        //
                        //  If IRS then convert buffer from EBCDIC to ASCII, output line and reset buffer pointer
                        //
                    } else if (chr == IRS) {
                        if (addFFtoEnd) {
                            buffer[bufferPointer] = 13;
                            buffer[bufferPointer + 1] = 21;
                            buffer[bufferPointer + 2] = 12;
                            bufferPointer = bufferPointer + 3;
                            addFFtoEnd = false;
                        }
                        String temp = "";
                        for (int i = 0; i < bufferPointer; i++) {
                            int index = buffer[i] & 0xff;
                            temp += ebcdic_to_ascii[index];
                        }

                        //
                        //  If this is a $HASP100 message, output to console instead of printer
                        //
                        if (firstPrint && temp.contains("$HASP")) {
                            temp = temp.replace("\r", "");
                            temp = temp.replace("\n", "");
                            updateStatus("<---" + temp);
                            ignorePrint = true;
                        } else {
                            //
                            //  Setup printer file
                            //
                            if (!ignorePrint) {
                                if (firstPrint) {
                                    updateStatus("RJE007S - Receiving print data");
                                    if (traceOn) {
                                        traceOut("Receiving print data");
                                    }
                                }
                                if (searchStart && temp.contains("START  JOB")) {
                                    int left = temp.indexOf("JOB");
                                    String jobNum = "J" + temp.substring(left + 4, left + 8).replaceAll(" ", "");
                                    String user = temp.substring(left + 10, left + 18).replaceAll(" ", "");
                                    fileName = defaultPrintDirectory + user + "_" + jobNum + ".prt";
                                    searchStart = false;

                                    //
                                    //  Open the printer file based on filename and output stored lines
                                    //
                                    if (printFrame == null) {
                                        printPunch = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true)));
                                        updateStatus("RJE007X - Print filename=" + fileName);
                                    }
                                    if (traceOn) {
                                        traceOut("fileName=" + fileName);
                                    }
                                    for (int i = 0; i < firstLineIndex; i++) {
                                        if (!removeExtraPrinterBlankLines || linesPrinted > 1) {
                                            if (printFrame == null) {
                                                //
                                                //  Put out to print file
                                                //
                                                printPunch.write(firstLines[i]);
                                            } else {
                                                //
                                                //  Put out to experimantal printer window
                                                //
                                                printTextArea.append(firstLines[i]);
                                            }
                                        }
                                        linesPrinted++;
                                    }
                                    firstLineIndex = 0;

                                }
                                if (searchStart) {
                                    if (traceOn) {
                                        traceOut(">>>>" + temp.replaceAll("\\n", "").replaceAll("\\r", ""));
                                    }
                                    firstLines[firstLineIndex] = temp;
                                    firstLineIndex++;
                                    if (firstLineIndex == firstLines.length) {
                                        fileName = defaultPrintDirectory + "UnknownUserUnknownJobNumber" + ".prt";
                                        searchStart = false;

                                        //
                                        //  Open the printer file based on filename and output stored lines
                                        //
                                        if (printFrame == null) {
                                            printPunch = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true)));
                                            updateStatus("RJE007X - Print fileName=" + fileName);
                                        }
                                        if (traceOn) {
                                            traceOut("fileName=" + fileName);
                                        }
                                        for (int i = 0; i < firstLineIndex; i++) {
                                            if (!removeExtraPrinterBlankLines || linesPrinted > 1) {
                                                if (printFrame == null) {
                                                    //
                                                    //  Put out to print file
                                                    //
                                                    printPunch.write(firstLines[i]);
                                                } else {
                                                    //
                                                    //  Put out to experimantal printer window
                                                    //
                                                    printTextArea.append(firstLines[i]);
                                                }
                                            }
                                            linesPrinted++;
                                        }
                                        firstLineIndex = 0;
                                    }
                                } else {
                                    if (!removeExtraPrinterBlankLines || linesPrinted > 1) {
                                        if (printFrame == null) {
                                            //
                                            //  Put out to print file
                                            //
                                            printPunch.write(temp);
                                        } else {
                                            //
                                            //  Put out to experimantal printer window
                                            //
                                            printTextArea.append(temp);
                                        }
                                    }
                                    linesPrinted++;
                                    if (traceOn) {
                                        traceOut("pr>>" + temp.replaceAll("\\n", "").replaceAll("\\r", ""));
                                    }
                                }
                                firstPrint = false;
                            }
                            //
                            //  If we are punching then put out punch card

                            //
                            if (outdev == OutDevice.punch) {
                                if (!removeExtraPunchCards || cardsPunched > 1) {
                                    if (!firstPunch) {
                                        if (punchFrame == null) {
                                            //
                                            //  Put out to punch file
                                            //
                                            printPunch.write(temp);
                                        } else {
                                            //
                                            //  Put out to experimental punch window
                                            //
                                            punchTextArea.append(temp + "/r/n");
                                        }
                                    } else {

                                    }
                                }
                                cardsPunched++;
                            }
                        }
                        //
                        //  Reset buffer pointer and update printer or punch counter
                        //
                        bufferPointer = 0;
                        if (outdev == OutDevice.printer) {
                            printLines++;
                        } else {
                            punchLines++;
                        }
                        //
                        //  If ETX or ETB then this is the end of the block, send ACK
                        //
                    } else if (chr == ETX || chr == ETB) {
                        sendAck();
                        //
                        //  If EOT then we are done printing or punching, send ACK and show idle and close printer/punch file
                        //
                    } else if (chr == EOT) {
                        //sendAck();    // Removed, Don't need to ACK EOT as it Causes error messages to be output.
                        ack = ACK0;
                        status = Stat.Idle;
                        if (traceOn) {
                            traceOut("End of printing or punching");
                        }
                        updateState("Idle");
                        if (!ignorePrint || outdev == OutDevice.punch) {
                            //
                            //  Flush and close the printer/punch file
                            //
                            if (outdev == OutDevice.printer) {
                                updateStatus("RJE007Y - Printer closed");
                                if (printFrame == null) {
                                    printPunch.flush();
                                    printPunch.close();
                                }
                                if (traceOn) {
                                    traceOut("Printer closed");
                                }
                            } else {
                                updateStatus("RJE007Z - Punch closed");
                                if (punchFrame == null) {
                                    printPunch.flush();
                                    printPunch.close();
                                }
                                if (traceOn) {
                                    traceOut("Punch closed");
                                }
                            }

                            //
                            //  Let user know printer/punch finished
                            //
                            if (outdev == OutDevice.printer) {
                                updateStatus("RJE008S - End of transmission, " + NumberFormat.getNumberInstance(Locale.US).format(printLines) + " lines printed");
                                if (traceOn) {
                                    traceOut(printLines + " lines printed");
                                }
                                //ignorePrint = false;
                            } else {
                                updateStatus("RJE009S - End of transmission, " + NumberFormat.getNumberInstance(Locale.US).format(punchLines) + " cards punched");
                                if (traceOn) {
                                    traceOut(punchLines + " cards punched");
                                }
                            }
                            searchStart = true;
                            fileName = "";
                            outdev = OutDevice.console;
                        }
                        ignorePrint = false;

                        //
                        //  If previous character was an escape then check for possible vertical form characters
                        //
                    } else if (backChr == ESC) {
                        bufferPointer = 0;
                        //
                        //  Check if ENQ, if so send an ACK (needs more work)
                        //
                        if (chr == ENQ) {
                            updateStatus("ENQ seen");
                            if (traceOn) {
                                traceOut("Enq seen");
                            }
                            //
                            //  Sending an ACK seems to cause confusion.  Removed for now
                            //
                            //stxSeen = false;
                            //sendAck();
                            //
                            //  Check if skip 1 line (/)
                            //
                        } else if ((chr & 0xFF) == 0x61) {
                            buffer[bufferPointer] = 13;
                            buffer[bufferPointer + 1] = 21;
                            bufferPointer = bufferPointer + 2;
                            //
                            //  Check if skip 2 lines (S)
                            //
                        } else if ((chr & 0xFF) == 0xE2) {
                            buffer[bufferPointer] = 13;
                            buffer[bufferPointer + 1] = 21;
                            buffer[bufferPointer + 2] = 13;
                            buffer[bufferPointer + 3] = 21;
                            bufferPointer = bufferPointer + 4;
                            //
                            //  Check if skip 3 lines (T)
                            //
                        } else if ((chr & 0xFF) == 0xE3) {
                            buffer[bufferPointer] = 13;
                            buffer[bufferPointer + 1] = 21;
                            buffer[bufferPointer + 2] = 13;
                            buffer[bufferPointer + 3] = 21;
                            buffer[bufferPointer + 4] = 13;
                            buffer[bufferPointer + 5] = 21;
                            bufferPointer = bufferPointer + 6;
                            //
                            //  Check if top of page (A)
                            //
                        } else if ((chr & 0xFF) == 0xC1) {
                            buffer[bufferPointer] = 13;
                            buffer[bufferPointer + 1] = 21;
                            bufferPointer = bufferPointer + 2;
                            addFFtoEnd = true;
                            //
                            //  Check if go to beginning of line (no vertical space) (M)
                            //
                        } else if ((chr & 0xFF) == 0xD4) {
                            bufferPointer = 0;
                            //
                            //  Unknown value, just single space
                            //
                        } else {
                            //System.out.println("Unknown, skip one line");
                            buffer[bufferPointer] = 13;
                            buffer[bufferPointer + 1] = 21;
                            bufferPointer = bufferPointer + 2;
                        }
                    } else {
                        //
                        //  Nothing special, add character to buffer (don't save ESC however)
                        //
                        if (chr != ESC) {
                            buffer[bufferPointer] = chr;
                            bufferPointer++;
                            //
                            //  If this is for the punch check if we have reached 80 characters, if so output the record
                            //
                            if (outdev == OutDevice.punch) {
                                if (bufferPointer >= 80) {
                                    String temp = "";
                                    for (int i = 0; i < bufferPointer; i++) {
                                        int index = buffer[i] & 0xff;
                                        temp += ebcdic_to_ascii[index];
                                    }
                                    if (traceOn) {
                                        traceOut("pu>>" + temp);
                                    }
                                    if (!removeExtraPunchCards || cardsPunched > 1) {
                                        //printPunch.write(temp + "\r");
                                        if (punchFrame == null) {
                                            //
                                            //  Put out to punch file
                                            //
                                            printPunch.write(temp + "\r");
                                        } else {
                                            //
                                            //  Put out to experimental punch window
                                            //
                                            punchTextArea.append(temp + "\r\n");
                                        }
                                    }
                                    cardsPunched++;
                                    punchLines++;
                                    bufferPointer = 0;
                                }
                            }
                        }
                    }

                    //
                    //  Remeber this character
                    //
                    backChr = chr;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MainLoop.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //
    //  Decode useful ebcdic characters used for tracing
    //
    String decodeBisync(byte chr) {
        String result;
        switch (chr) {
            case 0x01:
                result = "SOH";
                break;
            case 0x02:
                result = "STX";
                break;
            case 0x03:
                result = "ETX";
                break;
            case 0x10:
                result = "DLE";
                break;
            case 0x11:
                result = "DC1";
                break;
            case 0x12:
                result = "DC2";
                break;
            case 0x13:
                result = "DC3";
                break;
            case 0x14:
                result = "DC4";
                break;
            case 0x1e:
                result = "IRS";
                break;
            case 0x26:
                result = "ETB";
                break;
            case 0x27:
                result = "ESC";
                break;
            case 0x2d:
                result = "ENQ";
                break;
            case 0x32:
                result = "SYN";
                break;
            case 0x37:
                result = "EOT";
                break;
            case 0x3d:
                result = "NAK";
                break;
            case 0x40:
                result = "SPACE";
                break;
            case 0x61:
                result = "ACK1";
                break;
            case 0x70:
                result = "ACK2";
                break;
            default:
                result = Integer.toHexString(chr & 0xff) + " " + ebcdic_to_ascii[chr & 0xff];
                break;
        }

        return result;
    }

    //
    //  Log into MVS
    //
    static void logOntoMVS() {

        try {
            //
            //  Connect to remote MVS host
            //
            if (!connected) {
                //
                //  Check if user wants to created a SSH tunnel to the MVS system
                //
                if (createSSHTunnel) {
                    try {
                        JSch jsch = new JSch();
                        if (sshPassword == null || sshPassword.equals("")) {
                            updateStatus("RJE000I - Connecting to " + hostName + " port " + hostPort + " via SSH tunnel using .pem file.");
                            jsch.addIdentity(pemFile);  // If no password assume pemFile
                        } else {
                            updateStatus("RJE000I - Connecting to " + hostName + " port " + hostPort + " via SSH tunnel using password.");
                        }
                        JSch.setConfig("StrictHostKeyChecking", "no");
                        Session loSshSession = jsch.getSession(sshUsername, hostName, 22);
                        if (sshPassword != null && !sshPassword.equals("")) {
                            loSshSession.setPassword(sshPassword); // If password add it
                        }
                        loSshSession.connect();
                        int assignedPort = loSshSession.setPortForwardingL(38809, hostName, hostPort);
                        sock = new Socket(InetAddress.getLocalHost(), assignedPort);
                    } catch (JSchException | ConnectException e) {
                        if (MainLoop.status == Stat.Nolink || MainLoop.status == Stat.Shutdown1 || MainLoop.status == Stat.Shutdown2) {
                            //System.out.println(e.getLocalizedMessage());
                            //System.out.println(e.getMessage());
                            JOptionPane.showMessageDialog(null, "Unable to conntect to " + hostName + ":" + hostPort + " via SSH tunnel.");
                            return;
                        }
                    }
                    
                } else {
                    //
                    //  Connect directly to MVS system
                    //
                    updateStatus("RJE000I - Connecting to " + hostName + " port " + hostPort + " via direct connection.");
                    try {
                        hostAddress = InetAddress.getByName(hostName).getHostAddress();
                        sock = new Socket(hostAddress, hostPort);
                    } catch (IOException ex) {
                        if (MainLoop.status == Stat.Nolink || MainLoop.status == Stat.Shutdown1 || MainLoop.status == Stat.Shutdown2) {
                            JOptionPane.showMessageDialog(null, "Unable to conntect to " + hostName + ":" + hostPort + " via direct connection.");
                            return;
                        }
                    }
                }
                input = new DataInputStream(sock.getInputStream());
                output = new DataOutputStream(sock.getOutputStream());
                connected = true;
            }

            //
            //  Send inital opening request to MVS host
            //
            updateStatus("RJE010I - Host Name: " + hostName + " IP Address: " + hostAddress);
            updateStatus("RJE011I - Communication starting");
            updateHostStatus(hostName, "Connected");
            updateSystem("JES2");
            byte[] start = {SYN, SYN, SYN, ENQ};
            output.write(start);
            output.flush();
            if (traceOn) {
                traceOut("--->SYN SYN SYN ENQ");
            }
            bytesSent += start.length;
            status = Stat.InitiaWait;
            updateState("InitialWait");
            backChr = 0;

            //
            //  Start the main loop
            //
            if (!mainLoopActive) {
                mainLoopActive = true;
                (new Thread(new MainLoop())).start();

            }

        } catch (UnknownHostException ex) {
            Logger.getLogger(MainLoop.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (IOException ex) {
            Logger.getLogger(MainLoop.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    //
    //  Log out of MVS
    //
    static void traceOut(String text) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date date = new Date();
        System.out.println(dateFormat.format(date) + " " + text);
    }

    static void logOutOfMVS() {

        try {
            if (status != Stat.Idle) {
                updateStatus("RJE014E - Busy, try again later");
                return;
            }
            byte[] stop = {SYN, SYN, SYN, ENQ};
            output.write(stop);
            output.flush();
            if (traceOn) {
                traceOut("--->SYN SYN SYN ENQ");
            }
            bytesSent += stop.length;
            status = Stat.Shutdown1;
            updateState("Shutdown1");
            backChr = 0;
            commandLine = signoffString;

        } catch (IOException ex) {
            Logger.getLogger(MainLoop.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    //
    //  Send a JES2 command
    //
    static void sendCommand() {

        try {
            if (status != Stat.Idle && status != Stat.Shutdown1) {
                updateStatus("RJE014E - Busy, send command when idle");
                return;
            }
            byte[] loginS = {SYN, SYN, SYN, ENQ};
            if (traceOn) {
                traceOut("--->SYN SYN SYN ENQ");
            }
            output.write(loginS);
            output.flush();
            bytesSent += loginS.length;
            if (status != Stat.Shutdown1) {
                status = Stat.SendingCommand1;

            }
        } catch (IOException ex) {
            Logger.getLogger(MainLoop.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    //
    //  User has requested to send a file (card deck).  Request permission from MVS to send deck
    //
    static void sendFile(String filename) {
        try {
            updateStatus("RJE015I - Job being submitted to host, filename: " + filename);
            status = Stat.SendPending;
            byte[] start = {SYN, SYN, ENQ};
            if (traceOn) {
                traceOut("--->SYN SYN ENQ");
            }
            output.write(start);
            output.flush();
            bytesSent += start.length;
            jclFile = filename;

        } catch (IOException ex) {
            Logger.getLogger(MainLoop.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    //
    //  Update system window
    //
    static void updateSystem(String system) {
        java.awt.EventQueue.invokeLater(() -> {
            Rje.systemField.setText(system);
        });
    }

//
    //  Update state window
    //
    static void updateState(String state) {
        java.awt.EventQueue.invokeLater(() -> {
            Rje.stateField.setText(state);
            Rje.stateField.setBackground(Color.red);
            if (state.equals("Idle")) {
                Rje.stateField.setBackground(Color.green);
            }
            if (state.equals("Receiving") || state.equals("Sending")) {
                Rje.stateField.setBackground(Color.orange);

            }
            if (state.equals("Shutting Down")) {
                Rje.stateField.setBackground(Color.yellow);

            }
        });
    }

    //
    //  Update host status window
    //
    static void updateHostStatus(String host, String status) {
        java.awt.EventQueue.invokeLater(() -> {
            Rje.hostField.setText(host);
            Rje.statusField.setText(status);
        });
    }

    //
    //  Update the status windows
    //
    static void updateStatus(String text) {
        if (text == null) {
            text = "";
        }
        final String Text = text;
        java.awt.EventQueue.invokeLater(() -> {
            String oldText = Rje.mainWindow.getText();
            if (oldText == null) {
                oldText = "";
            }
            SimpleDateFormat fd = new SimpleDateFormat("HH:mm:ss");
            String date = fd.format(new Date());
            Rje.mainWindow.setText(oldText + date + " " + Text + "\r\n");
        });
    }

    //
    //  Send a ack based on last ack sent
    //
    static void sendAck() {
        try {
            byte[] ACK = {SYN, SYN, DLE, 0x00};
            String text;
            if (ack == ACK0) {
                ACK[3] = ACK0;
                text = "ACK0";
                ack = ACK1;
            } else {
                ACK[3] = ACK1;
                text = "ACK1";
                ack = ACK0;
            }
            output.write(ACK);
            output.flush();
            bytesSent += ACK.length;
            if (traceOn) {
                traceOut("--->SYN SYN DLE " + text);
            }

        } catch (IOException ex) {
            Logger.getLogger(MainLoop.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    //
    //  Get next byte received from MVS
    //
    static byte getFromHost() {
        try {
            while (true) {
                byte[] byt = new byte[1];
                input.read(byt);
                bytesReceived++;
                if (byt[0] != 0) {
                    return byt[0];
                }
            }
        } catch (IOException ex) {

            if (status == Stat.InitiaWait) {
                updateStatus("RJE000F - "
                        + ".* to remote system (" + ex.getLocalizedMessage() + ")");
                status = Stat.Nolink;
                return 0x00;
            }
            if (status == Stat.Nolink) {
                return 0x00;
            }
            updateStatus("RJE000F - Connection lost with remote system");
            status = Stat.Nolink;
            return 0x00;
        }
    }

    //
    //  Create a commnand (or record) to send to MVS.  Pad to 80 characters if requested
    //
    static byte[] createCommand(byte[] prefix, String command, byte suffix, boolean pad) {
        //
        //  Create an array of the correct size
        //
        byte[] packet;
        if (pad) {
            packet = new byte[prefix.length + 80 + 1];
        } else {
            packet = new byte[prefix.length + command.length() + 1];
        }
        int index = 0;

        //
        //  1st put the prefix in
        //
        for (int i = 0; i < prefix.length; i++) {
            packet[index] = prefix[i];
            index++;
        }

        //
        //  2nd Convert the command/record from ascii to ebcdic and add it in
        //
        for (int i = 0; i < command.length(); i++) {
            int j = command.charAt(i);
            packet[index] = (byte) ascii_to_ebcdic[j];
            index++;
        }

        //
        //  3rd Pad command if needed with blanks
        //
        if (pad) {
            int padSize = 80 - command.length();
            for (int i = 0; i < padSize; i++) {
                packet[index] = SPC;
                index++;
            }
        }

        //
        //  Lastly add the suffix in
        //
        packet[index] = suffix;
        index++;

        return packet;
    }

    //
    //  Convert Hex Characters to String Characters
    //
    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        for (int i = 0; i < hex.length() - 1; i += 2) {
            String Output;
            Output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(Output, 16);
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }
}
