package god.p2ptestproj;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Administrator on 2017/09/24 0024.
 */
public class MainActivityTest {
    @Test
    public void getLocalIpAddress() throws Exception {
        MainActivity test = new MainActivity();
        String ip = test.getLocalIpAddress();
        Assert.assertEquals("192.168.106",ip);
    }

    @Test
    public void getLocalMacAddress() throws Exception {
        MainActivity test = new MainActivity();
        String ip = test.getLocalMacAddress();
        Assert.assertEquals("D0:22:BE:C6:99:21",ip);
    }

    @Test
    public void connect() throws Exception {

    }

}