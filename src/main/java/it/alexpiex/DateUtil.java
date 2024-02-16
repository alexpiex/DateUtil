package it.alexpiex;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  * DateUtil gestione e formattazione date (Italiana e Inglese).
  *
  * @author Alessandro Pietrucci.
  * @version 3.0
  */
 public class DateUtil {

    public static String DEFAULT_DATA_PATTERN = "dd/MM/yyyy";
    public static final String BEGIN_DATE_YEAR = String.valueOf(beginDate());
    public static final String END_DATE_YEAR = String.valueOf(endDate());
    private static StringBuilder error = new StringBuilder();
    private static String[] MY_FORMAT_DATE = {"dd/MM/yyyy", "dd/MM/yyyy HH:mm:ss", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"};

    private static Pattern patternDate = Pattern.compile("[0-9]{1,2}[/\\.-][0-9]{1,2}[/\\.-][0-9]{4}");
    private static Pattern patternDateTime = Pattern.compile("[0-9]{1,2}[/\\.-][0-9]{1,2}[/\\.-][0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}");
    private static Pattern patternTime = Pattern.compile("[0-9]{2}:[0-9]{2}:[0-9]{2}");

    private static int _typeFormat;
    public static String _applyPattern ="";

    private enum Tipo {
        ITALIANO(1),
        INGLESE(2);

        private final int value;

        Tipo(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public static boolean isValid(String dateToValidate) {

        if(checkFormat(dateToValidate)){
             return checkDate(dateToValidate);
        } else {
            return false;
        }

    }

    public boolean isValidDate(Date date) {
        if (date.getYear() < 0) return false; // counting a.c.
        if (date.getMonth() > 12 || date.getMonth() < 1) return false;
        if (date.getDay() < 1 || (date.getMonth() == 2 && date.getDay() > 29 && isLeapYear(date.getYear())) || (date.getMonth() == 2 && date.getDay() > 28 && !isLeapYear(date.getYear())))
            return false;
        return true;
    }

    /**
     * Controlla il formato (Pattern) della data.
     * @param dateString
     * @return
     */
    private static boolean checkFormat(String dateString){

        if(isEmptyDate(dateString)){
            appendError("checkFormat(): La data inserita non puo\' essere vuota ");
            return false;
        }

        if(isEmpty(_applyPattern)){
            appendError("checkFormat(): Attenzione specificare il pattern ");
            return false;
        }

        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(_applyPattern);
            date = sdf.parse(dateString);
            if (!dateString.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            //ex.printStackTrace();
            appendError("checkFormat(), ParseException: "+ex.getMessage()+", Pattern: "+_applyPattern);

        }
        return date != null;
    }

    /**
     * NON UTILIZZATA
     * @return
     */
    private static SimpleDateFormat checkFormat(){
        Locale loc = null;
        SimpleDateFormat formatter = null;

        if(_typeFormat == 1){
            //Italiano
            _applyPattern = "dd/MM/yyyy HH:mm:ss";
            loc = new Locale("it", "IT");
            formatter = new SimpleDateFormat(_applyPattern, loc);
        } else if(_typeFormat == 2){
            //Inglese
            _applyPattern = "yyyy/MM/dd HH:mm:ss";
            loc = new Locale("en", "US");
            formatter = new SimpleDateFormat(_applyPattern, loc);
        } else {
            appendError("checkFormat(), error: _typeFormat non definito.");
            return null;
        }

        return formatter;

    }

    /**
     * Conmtrolla formalmente la data, esegue la validazione dei giorni e dei mese, controllo anno bisestile.
     *
     * @param dateString
     * @return
     */
    private static boolean checkDate(String dateString) {

        if(isEmptyDate(dateString)){
            appendError("checkDate(): La data inserita non puo\' essere vuota ");
            return false;
        }

        String[] dd = null;
        String[] data = null;

        if(isDateTime(dateString)){
            data = dateString.split(" ");
            dd = data[0].split("[/\\.-]");
        } else {
            dd = dateString.split("[/\\.-]");
            //data[1] = dateString; //time
        }

        int giorno = 0;
        int mese = 0;
        int anno = 0;

        if(dd[0].length()==2){
            giorno = Integer.parseInt(dd[0]);
            mese = Integer.parseInt(dd[1]);
            anno = Integer.parseInt(dd[2]);
            _typeFormat = Tipo.ITALIANO.getValue();

        } else if(dd[0].length()==4){
            anno =  Integer.parseInt(dd[0]);
            mese = Integer.parseInt(dd[1]);
            giorno = Integer.parseInt(dd[2]);
            _typeFormat = Tipo.INGLESE.getValue();
        } else {
            appendError("checkDate(): La data inserita non e\' valida ");
            return false;
        }

        boolean b =  mese < 0 ;
        b =     b || mese > 12 ;
        if (b) {
            appendError("checkDate(): Il mese inserito non e\' valido");
            return false;
        }

        int gMax = 31;

        if (mese == 2) gMax = (anno % 4 == 0 && anno % 100 != 0) ? 29 : 28;
        if (mese == 4 || mese == 6 || mese == 9 || mese == 11) gMax = 30;

        b =      giorno < 0 ;
        b = b || giorno > gMax ;
        if (b) {
            appendError("checkDate(): I giorni inseriti per il mese corrente non sono validi");
            return false;
        }

        // validazione ora
        if (data!=null && data.length > 1) {

            if(check(patternTime, data[1])){
                String[] ss = data[1].split(":");
                b = b || Integer.parseInt(ss[0]) < 0;
                b = b || Integer.parseInt(ss[0]) > 23;
                b = b || Integer.parseInt(ss[1]) < 0;
                b = b || Integer.parseInt(ss[1]) > 59;
                b = b || Integer.parseInt(ss[2]) < 0;
                b = b || Integer.parseInt(ss[2]) > 59;

                if (b) {
                    appendError("checkDate(): L\'ora inserita non e\' valida");
                    return false;
                }

                return true;

            } else {
                appendError("checkDate(): L\'ora inserita non e\' valida");
                return false;
            }

        } else {
            return true;
        }

    }

    /**
     * NON UTILIZZATA
     * @param data
     * @return
     */
     private static boolean isValidIta(final String data) {

        boolean flag = true;
        final StringTokenizer strTok = new StringTokenizer(data, "/");
        final int nrTok = strTok.countTokens();
        if (nrTok != 3) {
            appendError("isValidIta: La data passata non e\' nel formato italiano");
            return false;
        }
        final String giorno = strTok.nextToken();
        final String mese = strTok.nextToken();
        final String anno = strTok.nextToken();
        try {
            if ((giorno.length() != 2) || (mese.length() != 2) || (anno.length() != 4)) {
                appendError("isValidIta: Data non formattata correttamente");
                return false;
            }
        }
        catch (final NumberFormatException e) {
            appendError("isValidIta: Number Format Exception: "+e.getMessage());
            return false;
        }
        int maxDay = 31;
        final int G = Integer.parseInt(giorno);
        final int M = Integer.parseInt(mese);
        final int A = Integer.parseInt(anno);

        if((M < 1) || (M > 12)) {
            appendError("isValidIta: Attenzione controllare il mese inserito");
            return false;
        }
        if((mese.equals("04")) || (mese.equals("06")) || (mese.equals("09")) || (mese.equals("11"))) {
            maxDay = 30;

        } else {
            if (mese.equals("02")) {
                if ((A % 4) > 0) {
                    maxDay = 28;
                } else {
                    if (((A % 100) == 0) && ((A % 400) > 0)) {
                        maxDay = 28;
                    } else {
                        maxDay = 29;
                    }
                }
            }
        }
        if((G > maxDay) || (G < 1)) {
            appendError("isValidIta: Attenzione controllare che i giorni del mese siano esatti per gli anni bisestili");
            flag = false;
        }

        if(A <= 1900){
            appendError("isValidIta: La Data e\' minore o uguale al 1900");
            flag = false;
        }

        return flag;

    }

     /**
      * Ritorna una stringa contentente le ore per la data in input.
      *
      * @param data
      * @return
      */
     public static String dateHours(long data, String pattern) throws ParseException{

         if (data == 0) {
             appendError("dateHours(): Data inserita non valida");
             return null;
         }
         SimpleDateFormat sdf = null;

         if (isEmpty(pattern)) {
              sdf = new SimpleDateFormat(_applyPattern);
         } else {
              sdf = new SimpleDateFormat(pattern);
         }

         //trasformo il timestamp in stringa
         //SimpleDateFormat sdf = new SimpleDateFormat(pattern);
         String date_time = sdf.format(new Date(data));
         //recupero il secondo token che rappresenta le ore

         String hours ="";

         if(isDateTime(date_time)){
             String[] token_date = date_time.split(" ");
             hours = token_date[1];
         }

         return hours;
     }

    /**
     * Checks if is saturday or sunday.
     *
     * @param date the date
     *
     * @return true, if is saturday or sunday
     */
    public static boolean isSaturdayOrSunday(Date date) {
        boolean retBln = false;
        if(null!=date){
            int day = date.getDay();
            if(day ==0 || day ==6){
                retBln =true;
            }
        }
        return retBln;
    }

    /**
     * Maggiore di.
     *
     * @param firstDate the first date
     * @param secondDate the second date
     *
     * @return true, if greater than
     */
    public static boolean greaterThan(String firstDate, String secondDate) {

        if(isEmptyDate(firstDate) || isEmptyDate(secondDate) ){
            appendError("greaterThan(): Le date inserita non possono essere vuote");
            return false;
        }

        if(!isValid(firstDate) || !isValid(secondDate)){
            return false;
        }

        boolean result = false;
        if (null != firstDate && null != secondDate) {
            //returns true if the first date is after second date
            SimpleDateFormat df = new SimpleDateFormat(_applyPattern);
            try {
                // Get Date 1
                Date fDate = df.parse(firstDate);

                // Get Date 2
                Date sDate = df.parse(secondDate);

                if (fDate.after(sDate))
                    result = true;
            } catch (ParseException e) {
            }
        }
        return result;
    }

    /**
      * Minore o uguale.
      *
      * @param dataDa
      * @param dataA
      * @return
      */
     public static boolean lessThanOrEqual(Date firstDate, Date secondDate) {

         if (firstDate == null || secondDate == null) {
             //throw new IllegalArgumentException("Campi data  non validi!");
             appendError("greaterEqualThen(): Le date inserite non possono essere vuote");
             return false;
         }

         boolean result = false;
         // precondizione la data deve essere presente
         if (secondDate != null) {
             long first_date = firstDate.getTime();
             long second_date = secondDate.getTime();

             if (first_date <= second_date) {
                 result = true;
             }
         }
         return result;
     }

     /**
      *
      * Maggiore o uguale.
      *
      * @param dataDa
      * @param dataA
      * @return
      */
     public static boolean greaterThanOrEqual(Date firstDate, Date secondDate) {
         if (firstDate == null || secondDate == null) {
             //throw new IllegalArgumentException("Campi data  non validi!");
             appendError("greaterEqualThen(): Le date inserite non possono essere vuota");
             return false;
         }

         boolean result = false;

         if (firstDate != null) {
             long first_date = firstDate.getTime();
             long second_date = secondDate.getTime();

             if (first_date >= second_date) {
                 result = true;
             }
         }
         return result;
     }

     /**
      * Verifico che la data indicata in input sia maggior della data di inizio anno.
      * greater than the start of the year
      * @param date
      * @return
      */
     public static boolean greaterThanStartOfYear(String date) {

         if(isEmptyDate(date)){
             appendError("greaterThanStartOfYear(): La data inserita non puo\' essere vuota ");
             return false;
         }

         if(!isValid(date)){
             return false;
         }

         boolean result = false;
         SimpleDateFormat sdf = new SimpleDateFormat(_applyPattern);

         try {

             if (date.trim().length() == 0) {
                 result = true;
             } else {
                 long data = (sdf.parse(date)).getTime();
                 long default_date = (sdf.parse(BEGIN_DATE_YEAR)).getTime();
                 if (data > default_date) {
                     result = true;
                 }
             }
         } catch (ParseException e) {
             //throw new IllegalArgumentException("Il valore Data non puo' essere parsato!");
             appendError("greaterThanStartOfYear(), error: "+e.getMessage());
             return false;
         }

         return result;

     }

     /**
      * Verifica che la data sia minore o uguale alla data odierna.
      * less than or equal to today
      * @param date
      * @return
      */
     public static boolean lessThanOrEqualToday(String date) {

         if(isEmptyDate(date)){
             appendError("lessThanOrEqualToday(): La data inserita non puo\' essere vuota ");
             return false;
         }

         if(!isValid(date)){
             return false;
         }

         boolean result = false;
         SimpleDateFormat sdf = new SimpleDateFormat(_applyPattern);
         Calendar cal = Calendar.getInstance();

         try {
             if (date.trim().length() == 0) {
                 result = false;
             } else {
                 long data = (sdf.parse(date)).getTime();
                 long today_day = (sdf.parse(sdf.format(cal.getTime()))).getTime();
                 if (data <= today_day) {
                     result = true;
                 }
             }
         } catch (ParseException e) {
             appendError("lessThanOrEqualToday(), error: "+e.getMessage());
             return false;
         }

         return result;
     }


     /**
      *
      * Converte la data (long) in stringa secondo il pattern indicato.
      *
      * @param data data
      * @param pattern pattern
      * @return
      */
     public static String convToString(long data, String pattern) {
         String date_format = "";

         if (data == 0) {
             //throw new IllegalArgumentException("Pattern Data Non Valido");
             appendError("convLongToString(): Parametro data Non Valido: "+pattern);
             return null;
         }

         SimpleDateFormat sdf = null;

         if(!isEmpty(pattern)){
             sdf = new SimpleDateFormat(pattern);
         } else {
             sdf = new SimpleDateFormat(_applyPattern);
         }

         try {
             Date _date = new Date(data);
             date_format = sdf.format(_date);

         } catch (Exception e) {
             appendError("convLongToString(), error: "+e.getMessage());
             return null;
         }

         return date_format;
     }

     /**
      * Ritorna un oggetto timestamp a partire da un oggetto Date in input
      *
      * @param date
      * @return
      */
     public static Timestamp convToTimestamp(Date date) {

         if(date == null){
             appendError("convToTimestamp(): Data inserita non valida ");
             return null;
         }

         return new Timestamp(date.getTime());
     }

    public static Calendar convToCalendar(Date date) {

         if(date == null){
             appendError("convToCalendar(): Data inserita vuota ");
             return null;
         }

         Calendar cal = null;
         try {
             DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
             date = (Date) formatter.parse(date.toString());
             cal = Calendar.getInstance();
             cal.setTime(date);

         } catch (ParseException e) {

             appendError("convToCalendar(), error: "+e.getMessage());
             return null;
         }
         return cal;
     }

     /**
      * Converte la Data (Date) in Stringa.
      * @param dtmDate
      * @param formatstr
      * @return
      */
     public static String convToString(Date dtmDate, String formatstr) {

         if(dtmDate == null){
             appendError("convToString(): Data inserita vuota ");
             return null;
         }

         try {
             SimpleDateFormat sdf = null;
             if(!isEmpty(formatstr)){
                 sdf = new SimpleDateFormat(formatstr);
             } else {
                 sdf = new SimpleDateFormat(_applyPattern);
             }

             String dateString = sdf.format(dtmDate);
             return dateString;

         } catch (Exception e) {
             appendError("convToString(), error: "+e.getMessage());
             return null;
         }
     }

    /**
     * Converte la Data (Date) in Stringa.
     * @param dtmDate
     * @return
     */
    public static String convToString(Date dtmDate) {

        if(dtmDate == null){
            appendError("convToString(): Data inserita non valida: "+dtmDate);
            return null;
        }

        try {

            SimpleDateFormat sdf = new SimpleDateFormat(_applyPattern);
            String dateString = sdf.format(dtmDate);
            return dateString;

        } catch (Exception e) {
            appendError("convToString(), error: "+e.getMessage());
            return null;
        }
    }

    /**
     * Ritorna un oggetto "Date" da una stringa in input.
     *
     * @param date
     * @return
     */
    public static Date convToDate(String dateString) {

       /* if(isEmptyDate(dateString)){
            appendError("convString2Date(): La data inserita non puo\' essere vuota ");
            return null;
        }*/
        Date dataTime = null;

        if(!isValid(dateString)){
            return null;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(_applyPattern);

            try {
                formatter.setLenient(false);
                dataTime = new Date(formatter.parse(dateString).getTime());

            } catch (ParseException e){
                appendError("convToDate(), error: "+e.getMessage());
                return null;
            }
        }

        return dataTime;
    }

    /**
     * Formatta un DATE in una stringa per MySql.
     * @param date
     * @return
     */
    public static String convToMySQL(Date date) {

        if(date == null){
            appendError("formatAsMySQLDatetime(): La data inserita non pu\'o essere vuota ");
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
    }

    /**
     * Coverte in java.sql.Date
     * @param uData
     * @return
     */
    public static java.sql.Date convToSQLDate(Date uData){
        try {

            if(uData == null){
                appendError("toSQLDate(): Data inserita non valida: "+uData);
                return null;
            }

            java.sql.Date sDate = new java.sql.Date(uData.getTime());
            return sDate;

        } catch (Exception e) {
            appendError("toSQLDate() error toSQLDate(): "+e.getMessage());
            return null;
        }
    }

    /**
     * Convert millis to human readable time
     *
     * @param time Time string
     * @return Time String
     */
    public static long convTimeToMillis(String time) {
        String[] hhmmss = time.split(":");
        int hours = 0;
        int minutes;
        int seconds;
        if(hhmmss.length == 3) {
            hours = Integer.parseInt(hhmmss[0]);
            minutes = Integer.parseInt(hhmmss[1]);
            seconds = Integer.parseInt(hhmmss[2]);
        } else {
            minutes = Integer.parseInt(hhmmss[0]);
            seconds = Integer.parseInt(hhmmss[1]);
        }
        return (((hours * 60)+(minutes * 60) + seconds) * 1000);
    }

    /**
     * Metodo che controlla se una data è in un determinato range.
     *
     * @param startTime
     * @param endTime
     * @param checkTime
     * @return
     */
    public static boolean checkDateInRange(String startDate, String endDate, String checkDate){
        if(isEmptyDate(startDate) || isEmptyDate(endDate) || isEmptyDate(checkDate)){
            appendError("checkDateInRange(): Le date inserita non possono essere vuote");
            return false;
        }

        if(!isValid(startDate) || !isValid(endDate) || !isValid(checkDate) ){
            return false;
        }

        Long startTimestamp = translateDateToTimestamp(startDate);
        Long endTimestamp = translateDateToTimestamp(endDate);
        Long checkTimestamp = translateDateToTimestamp(checkDate);
        if (null == startTimestamp || null == endTimestamp || null == checkTimestamp){
            //throw new NullPointerException();
            appendError("checkDateInRange(), error: NullPointerException");
        }
        return startTimestamp <= checkTimestamp && checkTimestamp <= endTimestamp;
    }

    public static Date today() throws ParseException {
        Date today = null;
        SimpleDateFormat formatter = new SimpleDateFormat(_applyPattern);
        today = new Date(formatter.parse(todayString()).getTime());
        return today;
    }

    public static String todayString() {
         Date today = new Date(new Date().getTime());
         SimpleDateFormat sdf = new SimpleDateFormat(_applyPattern);
         return sdf.format(today.getTime());
    }

    /**
     * Restituisce la data di oggi in formato stringa, esempio: 05012024
     * @return
     */
    public static String currentString(){
        Calendar calendar = Calendar.getInstance();
        String gg = String.valueOf(calendar.get(Calendar.DATE));
        String mm = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        if(gg.length() < 2){
            gg = "0".concat(gg);
        }
        if(mm.length() < 2){
            mm = "0".concat(mm);
        }
        String dataOdierna = gg + mm + year;

        return dataOdierna;
    }

    public static String currentYear() {
         Calendar calendar = new GregorianCalendar();
         java.util.Date d = new java.util.Date();
         calendar.setTime(d);
         int anno = calendar.get(Calendar.YEAR);
         Integer i = new Integer(anno);
         return i.toString();
    }

    public static String currentTime() {
         GregorianCalendar calendar = new GregorianCalendar(Locale.ITALY);
         int ora = calendar.get(Calendar.HOUR_OF_DAY);
         int minuti = calendar.get(Calendar.MINUTE);
         return ora + "." + minuti;
    }

    public static String currentTimestamp() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss(S)");
        return sdf.format(cal.getTime());
    }

     /**
      * Restituisce la data di ieri.
      * @return
      */
    public static String yesterday() {
        SimpleDateFormat sdf = new SimpleDateFormat(_applyPattern);
        Calendar ieri = new GregorianCalendar();
        ieri.setTime(new Date(new Date().getTime()));
        ieri.add(Calendar.DATE, -1);
        String dateString = sdf.format(ieri.getTime());
        return dateString;
    }

    public static String lastDayOfMonth(Date data){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);
        int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return String.valueOf(day);
    }

     public static String addMonths(int mesi, String dataBase) throws ParseException {

         if(isEmptyDate(dataBase)){
             appendError("addMonths(): La data inserita non puo\' essere vuota ");
             return null;
         }

         if(!isValid(dataBase)){
             appendError("addMonths(): Data non valida");
             return null;
         }

         Date d = convToDate(dataBase);
         DateFormat df = new SimpleDateFormat(_applyPattern);
         Calendar calendar = Calendar.getInstance();

         calendar.setTime(d);
         calendar.add(Calendar.MONTH, 1);

         String dateString = df.format(calendar.getTime());
         //System.out.println(df.format(data.getTime()));
         return dateString;
     }

     public static String addDays(int giorni, String dataBase) throws ParseException {

         if(isEmptyDate(dataBase)){
             appendError("addDays(): La data inserita non puo\' essere vuota ");
             return null;
         }

         if(!isValid(dataBase)){
             appendError("addMonths(): La data non valida");
             return null;
         }

         Date d = convToDate(dataBase);

         SimpleDateFormat sdf = new SimpleDateFormat(_applyPattern);
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(d);
         calendar.add(Calendar.DATE, giorni);
         String dateString = sdf.format(calendar.getTime());

         return dateString;
     }

     /**
      * Metodo che restituisce il numero di giorni per mese e anno, controllo anno bisestile.
      *
      * @return
      * @param anno
      * @param mese
      */
     public static String dayMonthYear(String mese, String anno) {
         String retval = "";
         if (mese.length() == 1) {
             mese = "0" + mese;
         }
         if (mese.length() != 2 || anno.length() != 4) {
             //throw new IllegalArgumentException("Il mese o l'anno inseriti non sono validi");
             appendError("dayMonthYear(): Il mese o l\'anno inseriti non sono validi");
             return null;
         }
         if (mese.equals("11") || mese.equals("04") || mese.equals("06") || mese.equals("09")) {
             retval = "30";
         } else {
             if (mese.equals("02")) {
                 if (isLeapYear(Integer.parseInt(anno))) {
                     retval = "29";
                 } else {
                     retval = "28";
                 }
             } else {
                 retval = "31";
             }
         }
         return retval;
     }

     /**
      * Ottenere la differenza in giorni tra due date, restituisce un double.
      * @param data
      * @param data_ins
      * @return
      */
     public static double differenceInDays(String dataDa, String dataA) {

         if(isEmptyDate(dataDa) || isEmptyDate(dataA) ){
             appendError("differenceInDays(): La date inserite non possono essere vuote");
             return -1;
         }

         if(!isValid(dataDa)){
             appendError("differenceInDays(): La data (dataDa) non valida");
             return -1;
         }

         if(!isValid(dataA)){
             appendError("differenceInDays(): La data (dataA) non valida");
             return -1;
         }

         double gg = 0;
         try {
             SimpleDateFormat df = new SimpleDateFormat(_applyPattern);

             Date data1 = df.parse(dataDa);
             Date data2 = df.parse(dataA);
             GregorianCalendar c1 = new GregorianCalendar();
             c1.setTime(data1);
             GregorianCalendar c2 = new GregorianCalendar();
             c2.setTime(data2);
             long dallaDataMilliSecondi = c1.getTimeInMillis();
             long allaDataMilliSecondi = c2.getTimeInMillis();
             long millisecondiFraDueDate = allaDataMilliSecondi - dallaDataMilliSecondi;
             // conversione in giorni con la divisione intera
             double giorniFraDueDate_DivInt = millisecondiFraDueDate / 86400000;
             // conversione in giorni con la divisione reale
             double giorniFraDueDate_DivReal = millisecondiFraDueDate / 86400000.0;

             // conversione in giorni con arrotondamento
             //della divisione reale
             double giorniFraDueDate_DivRealRound = Math.round( millisecondiFraDueDate / 86400000.0 );
             gg = giorniFraDueDate_DivInt;


         } catch(Exception exc) {
             //throw new IllegalArgumentException("La data non e' formattata correttamente!", exc);
             appendError("differenceInDays(), error"+exc.getMessage());
             return -1;
         }
         return gg;
     }

    /**
     * Ottenere la differenza in giorni tra due date, restituisce un long.
     * @param uno
     * @param due
     * @return
     */
    public static long differenceInDays(Date uno, Date due) {

        if((uno==null) || (due==null) ){
            appendError("differenceInDays(): La date inserite non possono essere vuote");
            return -1;
        }

        try {
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(uno);
            c2.setTime(due);
            long giorni = (c2.getTime().getTime() - c1.getTime().getTime())    / (24 * 3600 * 1000);
            return giorni;

        } catch (Exception e) {
            appendError("differenceInDays() Error: "+e.getMessage());
            return -1;
        }
    }

    /**
     * Visualizza a video la data.
     * @param currentLocale
     */
    public static void displayDate(Locale currentLocale) {

        Date today;
        String result;
        SimpleDateFormat formatter;

        formatter = new SimpleDateFormat(_applyPattern, currentLocale);
        today = new Date();
        result = formatter.format(today);

        System.out.println("Locale: " + currentLocale.toString());
        System.out.println("Result: " + result);
    }

    /**
     * Restituisce la data inizio anno corrente.
     * @return
     */
    private static Date beginDate() {

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        return calendar.getTime();
    }

    /**
     * Restituisce la data fine anno corrente.
     * @return
     */
    private static Date endDate() {

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        return calendar.getTime();

    }

    /**
     * Controllo anno bisestile.
     * @param anno
     * @return
     */
    public static boolean isLeapYear(int anno) {
        boolean result = false;
        if (((anno % 4 == 0 && anno % 100 != 0) || anno % 400 == 0)) {
            result = true;
        }
        return result;
    }

    /**
     * Restituisce true se l'anno passato è bisestile.
     * @param year
     * @return
     */
    public boolean isBisestile(int year) {
        boolean isBisestile = false;
        int mod4 = year % 4;
        int mod100 = year % 100;
        int mod400 = year % 400;
        if(((mod4 == 0) && !(mod100 == 0)) || (mod400 == 0))
            isBisestile = true;
        return isBisestile;
    }

    /**
     * NON UTILIZZATA.
     *
     * @return String[]
     */
    private static String[] getdateformat() {
        //String[] formati = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd"};
        //Aggiungi a questa lista i formati da supportare (o rimuovi quelli non accettati)
        String[] formati = {
                "dd/MM/yyyy",
                "dd/MM/yyyy HH.mm.ss",
                "dd/MM/yyyy HH.mm",
                "yyyy/MM/dd",
                "yyyy/MM/dd HH.mm.ss",
                "yyyy/MM/dd HH.mm"
        };

        return formati;
    }

    public static Date getSystemDate() {
        return (new java.sql.Date(new java.util.Date().getTime()));
    }

    private static boolean isEmpty(String s) {
        if (s == null || s.trim().equals("") || s.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Il metodo ritorna true se la stringa date e' nulla oppure vuota, false altrimenti.
     *
     * @author Alessandro Pietrucci
     * @param d
     * @return
     */
    public static boolean isEmptyDate(String d) {
        if (d == null || d.trim().equals("nulldate") || d.trim().equals("01/01/0001") ||d.trim().equals("") || d.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getError() {

        if(hasErrors()){
            String[] lines = error.toString().split("#");
            String strOut = "";
            for(String s: lines){
                strOut += s + "<br />";
            }
            return strOut;
        } else {
            return "";
        }

    }

    public static boolean hasErrors(){
        return (error.toString().length()>0)?true:false;
    }

    private static void appendError(String str){
        error.append(str);
        error.append("#");
    }

    private static boolean check(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input.trim());
        return matcher.matches();

    }

    private static String normalizeDate(String data){
        String dat = null;
        dat = replaceInString(data,"-", "/");
        dat = replaceInString(dat,".", "/");

        return dat;
    }

    private static Long translateDateToTimestamp(String time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(_applyPattern);
        Date date = null;
        try {
            date = simpleDateFormat.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            //e.printStackTrace();
            appendError("translateDateToTimestamp(), error: "+e.getMessage());
        }
        return null;
    }

    /**
     * Sostituisce una stringa con un'altra per tutta la stringa di origine.
     *
     * @param in the source String
     * @param from the sub String to replace
     * @param to the sub String to replace with
     * @return a new String with all occurences of from replaced by to
     */
    private static String replaceInString(String in, String from, String to) {
        if (in == null || from == null || to == null) {
            return in;
        }

        StringBuffer newValue = new StringBuffer();
        char[] inChars = in.toCharArray();
        int inLen = inChars.length;
        char[] fromChars = from.toCharArray();
        int fromLen = fromChars.length;

        for (int i = 0; i < inLen; i++) {
            if (inChars[i] == fromChars[0] && (i + fromLen) <= inLen) {
                boolean isEqual = true;
                for (int j = 1; j < fromLen; j++) {
                    if (inChars[i + j] != fromChars[j]) {
                        isEqual = false;
                        break;
                    }
                }
                if (isEqual) {
                    newValue.append(to);
                    i += fromLen - 1;
                } else {
                    newValue.append(inChars[i]);
                }
            } else {
                newValue.append(inChars[i]);
            }
        }
        return newValue.toString();
    }


    /**
     * Indica se una determinata stringa rappresenta o meno una stringa di data e ora o una data semplice.
     *
     * @param dateString Date String
     * @return True if given string is a date time False otherwise
     */
    public static boolean isDateTime(String dateString) {
        return (dateString != null) && (dateString.trim().split(" ").length > 1);
    }

    public static int[] splitYMD(String date) {
        date = date.replace("-", "");

        String[]  dt = date.split("[/\\.-]");
        int Y=0, M=1, D=2;

        int[] ymd = { 0, 0, 0 };
        ymd[Y] = Integer.parseInt(date.substring(0, 4));
        ymd[M] = Integer.parseInt(date.substring(4, 6));
        ymd[D] = Integer.parseInt(date.substring(6, 8));
        return ymd;
    }

    public int getTypeFormat() {
        return _typeFormat;
    }

    public static String getDatePattern() {
        return _applyPattern;
    }

    public static void setDatePattern(String pattern) {
        _applyPattern = pattern;
    }




    /**
     * Ottenere la differenza in giorni tra due date, restituisce un long.
     * @param data1
     * @param data2
     * @return
     */
    /*public static long diffGGtraDate(Date data1, Date data2) {
    long dd = -1;
    if (data1 != null && data2 != null) {
    dd = (data1.getTime() - data2.getTime())/ 86400000;
    }
    return dd;
    }*/

    /**
     * NON UTILIZZATA
     * @param dateString
     * @param parsePatterns
     * @return
     */
/*     private static boolean isValid(String dateString, String[] parsePatterns){

         if(isEmptyString(dateString)){
             appendError("Il valore Data non puo\' essere null");
             return false;
         }
         int index = 0;
         Date parseDate = null;
         //ParseException throwe = null;
         SimpleDateFormat sdf = new SimpleDateFormat();

         while (parsePatterns != null && index < parsePatterns.length) {
             try {
                 sdf.applyPattern(parsePatterns[index]);
                 index++;
                 parseDate = sdf.parse(dateString);
                 break;
             } catch (ParseException px) {
                 //throwe = px;
                 continue;
             }
         }
         if (parseDate == null) {
             return false;
         }
         return true;

     }*/


/*    public static boolean isDateItalianValid_old(String dateTime) {

        dateTime = dateTime.trim();

        // controllo la data in formato italiano
        if (!patternDate.matcher(dateTime).matches() && !patternDateTime.matcher(dateTime).matches()) {
            //throw new ParseException("La data immessa non e' nel formato italiano (dd/MM/yyyy)", 0);
            appendError("La data inserita non e\' nel formato italiano (dd/MM/yyyy)");
            return false;
        }

        String[] data = dateTime.split(" ");
        String[] dd = data[0].split("[/\\.-]");

        int mese = Integer.parseInt(dd[1]);
        boolean b =  mese < 0 ;
        b =     b || mese > 12 ;
        if (b) {
            //throw new ParseException("La data immessa non e' valida", 0);
            appendError("Il mese inserito non e\' valido");
            return false;
        }

        int gMax = 31;

        int anno = Integer.parseInt(dd[2]);
        if (mese == 2) gMax = (anno % 4 == 0 && anno % 100 != 0) ? 29 : 28;
        if (mese == 4 || mese == 6 || mese == 9 || mese == 11) gMax = 30;

        b =      Integer.parseInt(dd[0]) < 0 ;
        b = b || Integer.parseInt(dd[0]) > gMax ;
        if (b) {
            //throw new ParseException("I giorni inseriti per il mese corrente non sono validi", 0);
            appendError("I giorni inseriti per il mese corrente non sono validi");
            return false;
        }

        // validazione dell'ora
        if (data.length > 1) {

            String[] ss = data[1].split(":");
            b = b || Integer.parseInt(ss[0]) < 0;
            b = b || Integer.parseInt(ss[0]) > 23;
            b = b || Integer.parseInt(ss[1]) < 0;
            b = b || Integer.parseInt(ss[1]) > 59;
            b = b || Integer.parseInt(ss[2]) < 0;
            b = b || Integer.parseInt(ss[2]) > 59;

            if (b) {
                //throw new ParseException("L'ora immessa non e' valida", 0);
                appendError("L\'ora inserita non e\' valida");
                return false;
            }

            return true;


        } else {
            return true;
        }
    }*/

    /*
     public static boolean isValid(String dateToValidate, String dateFormat){

         if(isEmpty(dateFormat)){
             appendError("isValid(): Pattern inserito non valido NULL");
             return false;
         }

         if(isEmptyDate(dateToValidate)){
             appendError("isValid(): Data inserita non valida");
             return false;
         }

         try {

             if(DEFAULT_DATA_PATTERN.equals(dateFormat)){
                 return isValidIta(dateToValidate);
             } else {
                 SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                 sdf.setLenient(false);
                 //if not valid, it will throw ParseException
                 Date date = sdf.parse(dateToValidate);
             }

         } catch (ParseException e) {
             appendError("isValid(), error: "+e.getMessage());
             return false;
         }

         return true;
     }
*/
    /**
     * Parse String to Date.
     *
     * @param dateString String of Date, the format is yyyy-MM-dd or yyyy/MM/dd
     * or yyyy.MM.dd
     * @return Date
     * @throws ParseException
     */
    /*
    public static Date stringToDate(String dateString) throws ParseException {
        String[] formatstring = getdateformat();

        Date parseDate = null;
        ParseException throwe = null;
        SimpleDateFormat sdf = new SimpleDateFormat();
        int index = 0;
        while (formatstring != null && index < formatstring.length) {
            try {
                System.out.println(formatstring[index]);
                sdf.applyPattern(formatstring[index]);
                index++;
                sdf.setLenient(false);
                parseDate = sdf.parse(dateString);
                break;
            } catch (ParseException px) {
                throwe = px;
                continue;
            }
        }
        if (parseDate == null) {
            //throw throwe;
            appendError("stringToDate(), error: "+throwe);
        }
        return parseDate;
    }


    public static Date stringToDate2(String dateString) throws ParseException {
        String[] formatstring = getdateformat();

        Date parseDate = null;
        ParseException throwe = null;
        SimpleDateFormat sdf = null;

        for (int k = 0; k < formatstring.length; k++) {
            try {
                System.out.println(formatstring[k]);
                //sdf.applyPattern(formatstring[k]);
                sdf = new SimpleDateFormat(formatstring[k], new Locale("it", "IT"));
                //dat = (Date) sdf.parse(data);
                sdf.setLenient(false);
                parseDate = new Date(sdf.parse(dateString).getTime());

                break;
            } catch (ParseException px) {
                throwe = px;
                continue;
            }
        }

        if (parseDate == null) {
            //throw throwe;
            appendError("stringToDate(), error: "+throwe);
        }
        return parseDate;
    }*/

 }
