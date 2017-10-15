package god.p2ptestproj;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2017/09/24 0024.
 */

public class SocketServer {

    private static final String TAG = "SocketSerevr";
    private ServerSocket server;
    private Socket socket;
    private InputStream in;
    private String str = null;
//    private boolean isClint;
    public static Handler ServerHandler;

    /**
     * initialize class should identify the port
     *
     * @param port input port
     */

    public SocketServer(int port) {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "initialize port error");
        }
    }

    public void startListen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //accept connection request
                    socket = server.accept();
                    try {
                        //get input stream
                        in = socket.getInputStream();
                        //looping message receive
                        while (!socket.isClosed()) {
                            //change string code format
                            byte[] bt = new byte[50];
                            try{
                                in.read(bt);
                                str = new String(bt, "UTF-8");
                            }catch(Exception e){

                            }
                            //return receiver message to main screen
                            if (!str.equals("")&&str!=null) {
                                returnMessage(str);
                            }
                            //TODO receiver Toast should delete while test is done
//                            Toast.makeText(, "Receive Message: " + str, Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * to send message from main screen
     *
     * @param chat main screen string
     */
    public void sendMessage(final String chat) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    out.print(chat);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Send Message IOError");
                }catch(Exception ee){

                }
            }
        }).start();
    }


    /**
     * reciver message and return to main screen
     *
     * @param str String needs to return
     */
    private void returnMessage(String str) {
        Message msg = new Message();
        msg.obj = str;
        ServerHandler.sendMessage(msg);

    }

}
