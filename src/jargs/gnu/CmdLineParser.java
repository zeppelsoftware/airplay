package jargs.gnu;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

public class CmdLineParser
{
  private String[] remainingArgs = null;
  private Hashtable options = new Hashtable(10);
  private Hashtable values = new Hashtable(10);

  public CmdLineParser() {}

  public final Option addOption(Option paramOption)
  {
    if (paramOption.shortForm() != null) {
      options.put("-" + paramOption.shortForm(), paramOption);
    }
    options.put("--" + paramOption.longForm(), paramOption);
    return paramOption;
  }

  public final Option addStringOption(char paramChar, String paramString)
  {
    return addOption(new CmdLineParser.Option.StringOption(paramChar, paramString));
  }

  public final Option addStringOption(String paramString)
  {
    return addOption(new CmdLineParser.Option.StringOption(paramString));
  }

  public final Option addIntegerOption(char paramChar, String paramString)
  {
    return addOption(new CmdLineParser.Option.IntegerOption(paramChar, paramString));
  }

  public final Option addIntegerOption(String paramString)
  {
    return addOption(new CmdLineParser.Option.IntegerOption(paramString));
  }

  public final Option addLongOption(char paramChar, String paramString)
  {
    return addOption(new CmdLineParser.Option.LongOption(paramChar, paramString));
  }

  public final Option addLongOption(String paramString)
  {
    return addOption(new CmdLineParser.Option.LongOption(paramString));
  }

  public final Option addDoubleOption(char paramChar, String paramString)
  {
    return addOption(new CmdLineParser.Option.DoubleOption(paramChar, paramString));
  }

  public final Option addDoubleOption(String paramString)
  {
    return addOption(new CmdLineParser.Option.DoubleOption(paramString));
  }

  public final Option addBooleanOption(char paramChar, String paramString)
  {
    return addOption(new CmdLineParser.Option.BooleanOption(paramChar, paramString));
  }

  public final Option addBooleanOption(String paramString)
  {
    return addOption(new CmdLineParser.Option.BooleanOption(paramString));
  }

  public final Object getOptionValue(Option paramOption)
  {
    return getOptionValue(paramOption, null);
  }

  public final Object getOptionValue(Option paramOption, Object paramObject)
  {
    Vector localVector = (Vector)values.get(paramOption.longForm());
    if (localVector == null) {
      return paramObject;
    }
    if (localVector.isEmpty()) {
      return null;
    }
    Object localObject = localVector.elementAt(0);
    localVector.removeElementAt(0);
    return localObject;
  }

  public final Vector getOptionValues(Option paramOption)
  {
    Vector localVector = new Vector();
    for (;;)
    {
      Object localObject = getOptionValue(paramOption, null);
      if (localObject == null) {
        return localVector;
      }
      localVector.addElement(localObject);
    }
  }

  public final String[] getRemainingArgs()
  {
    return remainingArgs;
  }

  public final void parse(String[] paramArrayOfString)
    throws CmdLineParser.IllegalOptionValueException, CmdLineParser.UnknownOptionException
  {
    parse(paramArrayOfString, Locale.getDefault());
  }

  public final void parse(String[] paramArrayOfString, Locale paramLocale)
    throws CmdLineParser.IllegalOptionValueException, CmdLineParser.UnknownOptionException
  {
    Vector localVector = new Vector();
    int i = 0;
    values = new Hashtable(10);
    while (i < paramArrayOfString.length)
    {
      String str1 = paramArrayOfString[i];
      if (str1.startsWith("-"))
      {
        if (str1.equals("--"))
        {
          i++;
          break;
        }
        String str2 = null;
        int j;
        if (str1.startsWith("--"))
        {
          j = str1.indexOf("=");
          if (j != -1)
          {
            str2 = str1.substring(j + 1);
            str1 = str1.substring(0, j);
          }
        }
        else if (str1.length() > 2)
        {
          for (j = 1; j < str1.length(); j++)
          {
            localObject = (Option)options.get("-" + str1.charAt(j));
            if (localObject == null) {
              throw new UnknownSuboptionException(str1, str1.charAt(j));
            }
            if (((Option)localObject).wantsValue()) {
              throw new NotFlagException(str1, str1.charAt(j));
            }
            addValue((Option)localObject, ((Option)localObject).getValue(null, paramLocale));
          }
          i++;
          continue;
        }
        Option localOption = (Option)options.get(str1);
        if (localOption == null) {
          throw new UnknownOptionException(str1);
        }
        Object localObject = null;
        if (localOption.wantsValue())
        {
          if (str2 == null)
          {
            i++;
            if (i < paramArrayOfString.length) {
              str2 = paramArrayOfString[i];
            }
          }
          localObject = localOption.getValue(str2, paramLocale);
        }
        else
        {
          localObject = localOption.getValue(null, paramLocale);
        }
        addValue(localOption, localObject);
        i++;
      }
      else
      {
        localVector.addElement(str1);
        i++;
      }
    }
    while (i < paramArrayOfString.length)
    {
      localVector.addElement(paramArrayOfString[i]);
      i++;
    }
    remainingArgs = new String[localVector.size()];
    localVector.copyInto(remainingArgs);
  }

  private void addValue(Option paramOption, Object paramObject)
  {
    String str = paramOption.longForm();
    Vector localVector = (Vector)values.get(str);
    if (localVector == null)
    {
      localVector = new Vector();
      values.put(str, localVector);
    }
    localVector.addElement(paramObject);
  }

  public static abstract class Option
  {
    private String shortForm = null;
    private String longForm = null;
    private boolean wantsValue = false;

    protected Option(String paramString, boolean paramBoolean)
    {
      this(null, paramString, paramBoolean);
    }

    protected Option(char paramChar, String paramString, boolean paramBoolean)
    {
      this(new String(new char[] { paramChar }), paramString, paramBoolean);
    }

    private Option(String paramString1, String paramString2, boolean paramBoolean)
    {
      if (paramString2 == null) {
        throw new IllegalArgumentException("Null longForm not allowed");
      }
      shortForm = paramString1;
      longForm = paramString2;
      wantsValue = paramBoolean;
    }

    public String shortForm()
    {
      return shortForm;
    }

    public String longForm()
    {
      return longForm;
    }

    public boolean wantsValue()
    {
      return wantsValue;
    }

    public final Object getValue(String paramString, Locale paramLocale)
      throws CmdLineParser.IllegalOptionValueException
    {
      if (wantsValue)
      {
        if (paramString == null) {
          throw new CmdLineParser.IllegalOptionValueException(this, "");
        }
        return parseValue(paramString, paramLocale);
      }
      return Boolean.TRUE;
    }

    protected Object parseValue(String paramString, Locale paramLocale)
      throws CmdLineParser.IllegalOptionValueException
    {
      return null;
    }

    public static class StringOption
      extends CmdLineParser.Option
    {
      public StringOption(char paramChar, String paramString)
      {
        super(paramString, true);
      }

      public StringOption(String paramString)
      {
        super(true);
      }

      protected Object parseValue(String paramString, Locale paramLocale)
      {
        return paramString;
      }
    }

    public static class DoubleOption
      extends CmdLineParser.Option
    {
      public DoubleOption(char paramChar, String paramString)
      {
        super(paramString, true);
      }

      public DoubleOption(String paramString)
      {
        super(true);
      }

      protected Object parseValue(String paramString, Locale paramLocale)
        throws CmdLineParser.IllegalOptionValueException
      {
        try
        {
          NumberFormat localNumberFormat = NumberFormat.getNumberInstance(paramLocale);
          Number localNumber = localNumberFormat.parse(paramString);
          return new Double(localNumber.doubleValue());
        }
        catch (ParseException localParseException)
        {
          throw new CmdLineParser.IllegalOptionValueException(this, paramString);
        }
      }
    }

    public static class LongOption
      extends CmdLineParser.Option
    {
      public LongOption(char paramChar, String paramString)
      {
        super(paramString, true);
      }

      public LongOption(String paramString)
      {
        super(true);
      }

      protected Object parseValue(String paramString, Locale paramLocale)
        throws CmdLineParser.IllegalOptionValueException
      {
        try
        {
          return new Long(paramString);
        }
        catch (NumberFormatException localNumberFormatException)
        {
          throw new CmdLineParser.IllegalOptionValueException(this, paramString);
        }
      }
    }

    public static class IntegerOption
      extends CmdLineParser.Option
    {
      public IntegerOption(char paramChar, String paramString)
      {
        super(paramString, true);
      }

      public IntegerOption(String paramString)
      {
        super(true);
      }

      protected Object parseValue(String paramString, Locale paramLocale)
        throws CmdLineParser.IllegalOptionValueException
      {
        try
        {
          return new Integer(paramString);
        }
        catch (NumberFormatException localNumberFormatException)
        {
          throw new CmdLineParser.IllegalOptionValueException(this, paramString);
        }
      }
    }

    public static class BooleanOption
      extends CmdLineParser.Option
    {
      public BooleanOption(char paramChar, String paramString)
      {
        super(paramString, false);
      }

      public BooleanOption(String paramString)
      {
        super(false);
      }
    }
  }

  public static class IllegalOptionValueException
    extends CmdLineParser.OptionException
  {
    private CmdLineParser.Option option;
    private String value;

    public IllegalOptionValueException(CmdLineParser.Option paramOption, String paramString)
    {
      super();
      option = paramOption;
      value = paramString;
    }

    public CmdLineParser.Option getOption()
    {
      return option;
    }

    public String getValue()
    {
      return value;
    }
  }

  public static class NotFlagException
    extends CmdLineParser.UnknownOptionException
  {
    private char notflag;

    NotFlagException(String paramString, char paramChar)
    {
      super("Illegal option: '" + paramString + "', '" + paramChar + "' requires a value");
      notflag = paramChar;
    }

    public char getOptionChar()
    {
      return notflag;
    }
  }

  public static class UnknownSuboptionException
    extends CmdLineParser.UnknownOptionException
  {
    private char suboption;

    UnknownSuboptionException(String paramString, char paramChar)
    {
      super("Illegal option: '" + paramChar + "' in '" + paramString + "'");
      suboption = paramChar;
    }

    public char getSuboption()
    {
      return suboption;
    }
  }

  public static class UnknownOptionException
    extends CmdLineParser.OptionException
  {
    private String optionName = null;

    UnknownOptionException(String paramString)
    {
      this(paramString, "Unknown option '" + paramString + "'");
    }

    UnknownOptionException(String paramString1, String paramString2)
    {
      super();
      optionName = paramString1;
    }

    public String getOptionName()
    {
      return optionName;
    }
  }

  public static abstract class OptionException
    extends Exception
  {
    OptionException(String paramString)
    {
      super();
    }
  }
}
