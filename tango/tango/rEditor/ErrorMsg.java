package tango.rEditor;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * ErrorMSG print error stacktrace to JGRError.log in the property "user.dir"
 * 
 * @author Markus Helbig RoSuDa 2003 - 2004
 */

public class ErrorMsg {

	/**
	 * Create new ErrorMsg which will be appended to JGRError.log file.
	 * 
	 * @param e
	 *            Exception to add
	 */
	public ErrorMsg(Exception e) {
		String filename = "JGRError.log";
		String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		// e.printStackTrace();
		String error = "--------------------------------------\n\n";
		Calendar cal = new GregorianCalendar();

		// Get the components of the time
		int hour24 = cal.get(Calendar.HOUR_OF_DAY); // 0..23
		int min = cal.get(Calendar.MINUTE); // 0..59
		int year = cal.get(Calendar.YEAR); // 2002
		int month = cal.get(Calendar.MONTH); // 0=Jan, 1=Feb, ...
		int day = cal.get(Calendar.DAY_OF_MONTH); // 1...

		error += day + "." + months[month] + "." + year + "  " + hour24 + ":" + min + "\n\n";
		error += "Message : " + e.getMessage() + "\n\n";
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
			out.write(error);
			out.flush();
			e.printStackTrace(out);
			out.flush();
			out.write("\n\n--------------------------------------\n\n");
			out.flush();
			out.close();
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	public ErrorMsg(String msg) {
		String filename = "JGRError.log";
		String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		// e.printStackTrace();
		String error = "--------------------------------------\n\n";
		Calendar cal = new GregorianCalendar();

		// Get the components of the time
		int hour24 = cal.get(Calendar.HOUR_OF_DAY); // 0..23
		int min = cal.get(Calendar.MINUTE); // 0..59
		int year = cal.get(Calendar.YEAR); // 2002
		int month = cal.get(Calendar.MONTH); // 0=Jan, 1=Feb, ...
		int day = cal.get(Calendar.DAY_OF_MONTH); // 1...

		error += day + "." + months[month] + "." + year + "  " + hour24 + ":" + min + "\n\n";
		error += "Message : " + msg + "\n\n";
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
			out.write(error);
			out.flush();
			out.flush();
			out.write("\n\n--------------------------------------\n\n");
			out.flush();
			out.close();
		} catch (IOException err) {
			err.printStackTrace();
		}
	}
}