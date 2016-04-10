package uk.co.neuralcubes.neuralates.muse;

import com.google.common.eventbus.EventBus;
import com.interaxon.libmuse.Muse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import uk.co.neuralcubes.neuralates.muse.MuseHandler;

/**
 * Created by javi on 10/04/16.
 */
@RunWith(PowerMockRunner.class)
public class MuseHandlerTest {
    @Mock
    Muse muse;
    private EventBus eventBus;
    private MuseHandler museHandler;

    @Before
    public void setUp(){
        this.eventBus = new EventBus();
        this.museHandler = new MuseHandler(this.muse,this.eventBus);
    }


    /**
     * Verify that we connect to the muse hardware in an
     * asynchronous fashion
     */
    @Test
    public void testConnect(){
        this.museHandler.connect();
        Mockito.verify(this.muse).runAsynchronously();
    }

    @Test
    public void testSetConnectionListener() throws Exception {

    }
}