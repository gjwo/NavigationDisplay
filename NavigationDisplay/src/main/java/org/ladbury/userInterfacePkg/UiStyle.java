package org.ladbury.userInterfacePkg;

import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

//import org.ladbury.smartpowerPkg.Timestamped;
/**
 * This class holds styling information applicable to the User Interface
 * 
 * @author GJWood
 * @version 1.0 2012/12/01	Initial implementation
 */
public class UiStyle {
	//font definitions
	public final static Font TITLE_FONT = new java.awt.Font("SansSerif", Font.BOLD, 12);
	public final static Font NORMAL_FONT = new java.awt.Font("SansSerif", Font.PLAIN, 10);
	public final static Font BUTTON_FONT = new java.awt.Font("SansSerif", Font.PLAIN, 10);
	//date format
//	public final static DateFormat timestampFormat = new SimpleDateFormat(Timestamped.OUTPUTDATEFORMAT);
	public final static DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	public final static DateFormat dateFormat = new SimpleDateFormat("DD/MM/yy");
	//static strings for forms
	public static final String EMPTY = "<Empty>";
	public static final String NONE = "<None>";
	public static final String UNNAMED = "<Unnamed>";
	public static final String CREATE_NEW_ENTRY = "Create a new entry";
	public enum UiDialogueType {SINGLE_FORM, SINGLE_TABLE, TABBED_FORM, TABBED_TABLE}
}
