/**
* Product Name : Driver RX-TX (includes MIPS support)
*
* Copyright c 2014 Orange
* Reason: RXTX is a native library providing serial and parallel communication 
* for the Java Development Toolkit (JDK). The support for MIPS32 support is 
* added in this version (i.e. add mips, and META-INF folders). Initial RXTX 
* version is 2.1-7.
* License: This software is distributed under the LGPL v 2.1.
*
* A copy of the LGPL v 2.1 may be found at 
* http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html on November 21st 2007
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package gnu.io;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * @author heng
 * activator of bundle
 *  
 * it provides the static methods for recording the log
 * these methods are called by others classes and native C library 
 *
 */
public class RXTXlog implements BundleActivator, ServiceListener
{
	
	private BundleContext m_context = null;
	// service ref being used, pointing to the logService
	private ServiceReference m_ref = null;
	// service object being used
	private static LogService m_logService = null;
	
	/* set debug mode*/
	private static boolean DEBUG = false;
	
	public void start(BundleContext context) throws Exception 
	{
		m_context = context;
		
		// synchronized while registering the service listener and 
		// performing the initial log service lookup since we don't want to 
		// receive service events at this moment 
		synchronized (this)
		{
			m_context.addServiceListener(this, "(&(objectClass=" + LogService.class.getName() + "))");
			
			ServiceReference ref = m_context.getServiceReference(LogService.class.getName());
			
			if (ref != null)
			{
				m_ref = ref;
				m_logService = (LogService)m_context.getService(m_ref);
			}
						
		}
		
		printInfo(" rxtx start ");
		
	}

	public void stop(BundleContext context) throws Exception 
	{
		// NOTE: The service is automatically released.
	}

	public synchronized void serviceChanged(ServiceEvent event) 
	{		
		// if a logservice was registered, see if we need one, if so, get a reference to it
		if (event.getType() == ServiceEvent.REGISTERED)
		{
			if(m_ref == null)
			{
				// Get a reference to the service object
				m_ref = event.getServiceReference();
				m_logService = (LogService) m_context.getService(m_ref);				
			}
		}
		
		// if a logservice was unregisered, check if it was the one we were using, if so, 
		// unget the service and try to query to get another one 
		else if (event.getType() == ServiceEvent.UNREGISTERING)
		{
			if(event.getServiceReference() == m_ref)
			{
				// Unget service object and null references
				m_context.ungetService(m_ref);
				m_ref = null;
				m_logService = null;
				
				// Query if we can get another one
				ServiceReference ref = null;
				ref = m_context.getServiceReference(LogService.class.getName());
				if(ref != null)
				{
					m_ref = ref;
					m_logService = (LogService)m_context.getService(m_ref);
				}
			}
		}
	}
	
	
	/**
	 * show debug with the logger service or only on standard output if it doesn't
	 * started
	 * 
	 * @param message
	 */
	public static void printDebug(String message) {
		if(DEBUG){
			printMessage(LogService.LOG_DEBUG, message);
		}
	}

	/**
	 * show error with the logger service or only on standard output if it doesn't
	 * started
	 * 
	 * @param message
	 */  
	public static void printError(String message) {
		printMessage(LogService.LOG_ERROR, message);
	}
	
	/**
	 * show warning with the logger service or only on standard output if it doesn't
	 * started
	 * 
	 * @param message
	 */  
	public static void printWarning(String message) {
		printMessage(LogService.LOG_WARNING, message);
	}

	/**
	 * show Info with the logger service or only on standard output if it doesn't
	 * started
	 * 
	 * @param message
	 */
	public static void printInfo(String message) {
		printMessage(LogService.LOG_INFO, message);
	}

	  /**
	   * print message with the specified level.
	   * 
	   * @param level
	   * @param message
	   */
	private static void printMessage(int level, String message) {
		if (m_logService != null) {
			m_logService.log(level, message);
		}
	}

}
