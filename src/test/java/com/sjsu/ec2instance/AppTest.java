package com.sjsu.ec2instance;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 

    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */

    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     * @throws InterruptedException 
     */
    public void testApp() throws InterruptedException
    {
        assertTrue( true );
        EC2Operations c = new EC2Operations();
       // c.create("instance1");
        c.updateInstance("instance2");
    }
}
