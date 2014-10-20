/*-------------------------------------------------------------------------
|   rxtx is a native interface to serial ports in java.
|   Copyright 1997-2004 by Trent Jarvi taj@www.linux.org.uk
|
|   This library is free software; you can redistribute it and/or
|   modify it under the terms of the GNU Library General Public
|   License as published by the Free Software Foundation; either
|   version 2 of the License, or (at your option) any later version.
|
|   This library is distributed in the hope that it will be useful,
|   but WITHOUT ANY WARRANTY; without even the implied warranty of
|   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
|   Library General Public License for more details.
|
|   You should have received a copy of the GNU Library General Public
|   License along with this library; if not, write to the Free
|   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
--------------------------------------------------------------------------*/
package  gnu.io;

import  java.io.FileDescriptor;
import  java.util.Vector;
import  java.util.Enumeration;

/**
* @author Trent Jarvi
* @version %I%, %G%
* @since JDK1.0
*/

public class CommPortIdentifier extends Object /* extends Vector? */
{
	public static final int PORT_SERIAL   = 1;  // rs232 Port
//	public static final int PORT_PARALLEL = 2;  // Parallel Port
//	public static final int PORT_I2C      = 3;  // i2c Port
//	public static final int PORT_RS485    = 4;  // rs485 Port
//	public static final int PORT_RAW      = 5;  // Raw Port
	private String PortName;
	private boolean Available = true;    
	private String Owner;    
	private CommPort commport;
	private CommDriver RXTXDriver;
 	static CommPortIdentifier   CommPortIndex;
	CommPortIdentifier next;
	private int PortType;
//	private final static boolean debug = false;
	static Object Sync;
	Vector ownershipListener;



/*------------------------------------------------------------------------------
	static {}   aka initialization
	accept:       -
	perform:      load the rxtx driver
	return:       -
	exceptions:   Throwable
	comments:     static block to initialize the class
------------------------------------------------------------------------------*/
	// initialization only done once....
	static 
	{
		RXTXlog.printDebug("CommPortIdentifier:static initialization()");
		Sync = new Object();
		try 
		{
			CommDriver RXTXDriver = (CommDriver) Class.forName("gnu.io.RXTXCommDriver").newInstance();
			RXTXDriver.initialize();
		} 
		catch (Throwable e) 
		{
			RXTXlog.printError(e + " thrown while loading " + "gnu.io.RXTXCommDriver");
		}

		String OS;

		OS = System.getProperty("os.name");
		if(OS.toLowerCase().indexOf("linux") == -1)
		{
			RXTXlog.printDebug("Have not implemented native_psmisc_report_owner(PortName)); in CommPortIdentifier");
		}
		System.loadLibrary( "rxtxSerial" );
	}
	public CommPortIdentifier ( String pn, CommPort cp, int pt, CommDriver driver) 
	{
		PortName        = pn;
		commport        = cp;
		PortType        = pt;
		next            = null;
		RXTXDriver      = driver;

	}

/*------------------------------------------------------------------------------
	addPortName()
	accept:         Name of the port s, Port type, 
                        reverence to RXTXCommDriver.
	perform:        place a new CommPortIdentifier in the linked list
	return: 	none.
	exceptions:     none.
	comments:
------------------------------------------------------------------------------*/
	public static void addPortName(String s, int type, CommDriver c) 
	{ 

		RXTXlog.printDebug("CommPortIdentifier:addPortName("+s+")");
		AddIdentifierToList(new CommPortIdentifier(s, null, type, c));
	}
/*------------------------------------------------------------------------------
	AddIdentifierToList()
	accept:        The cpi to add to the list. 
	perform:        
	return: 	
	exceptions:    
	comments:
------------------------------------------------------------------------------*/
	private static void AddIdentifierToList( CommPortIdentifier cpi)
	{
		RXTXlog.printDebug("CommPortIdentifier:AddIdentifierToList()");
		synchronized (Sync) 
		{
			if (CommPortIndex == null) 
			{
				CommPortIndex = cpi;
				RXTXlog.printDebug("CommPortIdentifier:AddIdentifierToList() null");
			}
			else
			{ 
				CommPortIdentifier index  = CommPortIndex; 
				while (index.next != null)
				{
					index = index.next;
					RXTXlog.printDebug("CommPortIdentifier:AddIdentifierToList() index.next");
				}
				index.next = cpi;
			} 
		}
	}
/*------------------------------------------------------------------------------
	addPortOwnershipListener()
	accept:
	perform:
	return:
	exceptions:
	comments:   
------------------------------------------------------------------------------*/
	public void addPortOwnershipListener(CommPortOwnershipListener c) 
	{ 
		RXTXlog.printDebug("CommPortIdentifier:addPortOwnershipListener()");

		/*  is the Vector instantiated? */

		if( ownershipListener == null )
		{
			ownershipListener = new Vector();
		}

		/* is the ownership listener already in the list? */

		if ( ownershipListener.contains(c) == false)
		{
			ownershipListener.addElement(c);
		}
	}
/*------------------------------------------------------------------------------
	getCurrentOwner()
	accept:
	perform:
	return:
	exceptions:
	comments:    
------------------------------------------------------------------------------*/
	public String getCurrentOwner() 
	{ 
		RXTXlog.printDebug("CommPortIdentifier:getCurrentOwner()");
		return( Owner );
	}
/*------------------------------------------------------------------------------
	getName()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	public String getName() 
	{ 
		RXTXlog.printDebug("CommPortIdentifier:getName()");
		return( PortName );
	}
/*------------------------------------------------------------------------------
	getPortIdentifier()
	accept:
	perform:
	return:
	exceptions:
	comments:   
------------------------------------------------------------------------------*/
	static public CommPortIdentifier getPortIdentifier(String s) throws NoSuchPortException 
	{ 
		RXTXlog.printDebug("CommPortIdentifier:getPortIdentifier(" + s +")");
		CommPortIdentifier index = CommPortIndex;

		synchronized (Sync) 
		{
			while (index != null) 
			{
				if (index.PortName.equals(s)) break;
				index = index.next;
			}
		}
		if (index != null) return index;
		else
		{
			RXTXlog.printDebug("not found!" + s);
			throw new NoSuchPortException("from CommPortIdentifier:getPortIdentifier(string)");
		}
	}
/*------------------------------------------------------------------------------
	getPortIdentifier()
	accept:
	perform:
	return:
	exceptions:
	comments:    
------------------------------------------------------------------------------*/
	static public CommPortIdentifier getPortIdentifier(CommPort p) 
		throws NoSuchPortException 	
	{ 
		RXTXlog.printDebug("CommPortIdentifier:getPortIdentifier(CommPort)");
		CommPortIdentifier c = CommPortIndex;
		synchronized( Sync )
		{
			while ( c != null && c.commport != p )
				c = c.next;
		}
		if ( c != null )
			return (c);

		RXTXlog.printDebug("not found!" + p.getName());
		throw new NoSuchPortException("from CommPortIdentifier:getPortIdentifier(CommPort)");
	}
/*------------------------------------------------------------------------------
	getPortIdentifiers()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	static public Enumeration getPortIdentifiers() 
	{ 
		RXTXlog.printDebug("static CommPortIdentifier:getPortIdentifiers()");
		CommPortIndex = null;
		try 
		{
			CommDriver RXTXDriver = (CommDriver) Class.forName("gnu.io.RXTXCommDriver").newInstance();
			RXTXDriver.initialize();
		} 
		catch (Throwable e) 
		{
			RXTXlog.printError(e + " thrown while loading " + "gnu.io.RXTXCommDriver");
		}
		return new CommPortEnumerator();
	}
/*------------------------------------------------------------------------------
	getPortType()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	public int getPortType() 
	{ 
		RXTXlog.printDebug("CommPortIdentifier:getPortType()");
		return( PortType );
	}
/*------------------------------------------------------------------------------
	isCurrentlyOwned()
	accept:
	perform:
	return:
	exceptions:
	comments:    
------------------------------------------------------------------------------*/
	public synchronized boolean isCurrentlyOwned() 
	{ 
		RXTXlog.printDebug("CommPortIdentifier:isCurrentlyOwned()");
		return(!Available);
	}
/*------------------------------------------------------------------------------
	open()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	public synchronized CommPort open(FileDescriptor f) throws UnsupportedCommOperationException 
	{ 
		RXTXlog.printDebug("CommPortIdentifier:open(FileDescriptor)");
		throw new UnsupportedCommOperationException();
	}
	private native String native_psmisc_report_owner(String PortName);
	
	private native int testFileExistence(String filename);

/*------------------------------------------------------------------------------
	open()
	accept:      application makeing the call and milliseconds to block
                     during open.
	perform:     open the port if possible
	return:      CommPort if successfull
	exceptions:  PortInUseException if in use.
	comments:
------------------------------------------------------------------------------*/
//	private boolean HideOwnerEvents;

	public synchronized CommPort open(String TheOwner, int i) 
		throws gnu.io.PortInUseException, gnu.io.NoSuchPortException
	{ 
		RXTXlog.printDebug("CommPortIdentifier:open("+TheOwner + ", " +i+")");
		if (Available == false)
		{
			synchronized (Sync)
			{
				fireOwnershipEvent(CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED);
				try
				{
					wait(i);
				}
				catch ( InterruptedException e ) { }
			}
		}
		if (Available == false)
		{
			throw new gnu.io.PortInUseException(getCurrentOwner());
		}
		if(commport == null)
		{
			commport = RXTXDriver.getCommPort(PortName,PortType);
		}
		if(commport != null)
		{
			Owner = TheOwner;
			Available=false;
			fireOwnershipEvent(CommPortOwnershipListener.PORT_OWNED);
			return commport;
		}
		else
		{
			/*testFileExistence return 0, if the device file corresponds to PortName not exist*/
			if(testFileExistence(PortName)== 0)
				throw new gnu.io.NoSuchPortException("CommPortIdentifier open(): device on the Port:"+PortName+" is left");
			throw new gnu.io.PortInUseException("PortInUseException from CommPortIdentifier open(): "+
				native_psmisc_report_owner(PortName));
		}
	}
/*------------------------------------------------------------------------------
	removePortOwnership()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	public void removePortOwnershipListener(CommPortOwnershipListener c) 
	{ 
		RXTXlog.printDebug("CommPortIdentifier:removePortOwnershipListener()");
		/* why is this called twice? */
		if(ownershipListener != null)
			ownershipListener.removeElement(c);
	}

/*------------------------------------------------------------------------------
	internalClosePort()
	accept:       None
	perform:      clean up the Ownership information and send the event
	return:       None
	exceptions:   None
	comments:     None
------------------------------------------------------------------------------*/
	synchronized void internalClosePort() 
	{
		RXTXlog.printDebug("CommPortIdentifier:internalClosePort()");
		Owner = null;
		Available = true;
		commport = null;
		/*  this tosses null pointer?? */
		notifyAll();
		fireOwnershipEvent(CommPortOwnershipListener.PORT_UNOWNED);
	}
/*------------------------------------------------------------------------------
	fireOwnershipEvent()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	void fireOwnershipEvent(int eventType)
	{
		RXTXlog.printDebug("CommPortIdentifier:fireOwnershipEvent( " + eventType + " )");
		if (ownershipListener != null)
		{
			CommPortOwnershipListener c;
			for ( Enumeration e = ownershipListener.elements();
				e.hasMoreElements(); 
				c.ownershipChange(eventType))
				c = (CommPortOwnershipListener) e.nextElement();
		}
	}
}

