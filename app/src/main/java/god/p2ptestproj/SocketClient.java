package god.p2ptestproj;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Harry on 2017/09/24 0024.
 */

public class SocketClient {
    private Socket client;
    private Context context;
    private int port;           //IP
    private String site;            //Port
    public static Handler mHandler;
    private boolean isClient;
    private PrintWriter out;
    private InputStream in;
    private String str;
    static private final String TAG = "Socket Client";

    public SocketClient(Context context, String site, int port) {
        this.context = context;
        this.site = site;
        this.port = port;
    }


    /**
     * open the Client thread to build connection
     */
    public void startClientThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new Socket(site, port);
                    isClient = true;
                    forOut();
                    forIn();

                } catch (UnknownHostException e) {
                    isClient = false;
//                    Toast.makeText(context, "Cannot connect target server", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                } catch (IOException e) {
                    isClient = false;
//                    Toast.makeText(context, "Cannot connect target server", Toast.LENGTH_LONG).show();
                    Log.i(TAG, e.toString());
                }
            }
        }).start();
    }

    /**
     * read the input steam
     */
    private void forIn() {
        while (isClient) {
            try {
                in = client.getInputStream();

                /* get s 16 bits String needs to convert*/
                byte[] bt = new byte[50];
                in.read(bt);
                str = new String(bt, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str != null){
                Message msg = new Message();
                msg.obj = str;
                mHandler.sendMessage(msg);
            }
        }
    }

    /**
     * read the out put stream
     */
    private void forOut() {
        try {
            out = new PrintWriter(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Get out put Stream fill" + e.toString());
        }
    }

    /**
     * send message
     */
    public void sendMessage(final String str) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (client != null) {
                    out.print(str);
                    out.flush();
                    Log.i(TAG, out + "");
                } else {
                    isClient = false;
                    Toast.makeText(context, "Network connect fill", Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }

}
