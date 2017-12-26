/*
 * Copyright 2011 Sistemas Informacion Satelital S.A. de C.V.
 *
 * All Rights reserved. Offenders will be held liable for payment of damages.
 * All rights created by patent grant or registration of a utility model or
 * design patent are reserved.
 *
 * @author Beni Barbosa
 * @date 09/05/2017
 * @version 1.0
 * @document SaraWatchDog.java
 */

package sarawatchdog;

import com.sis.sara.wdmail.util.DbConnection;
import com.sis.sara.wdmailDAO.DayReportsDAO;
import com.sis.sara.wdmailDAO.MailNotReceivedDAO;
import com.sis.sara.wdmailDAO.ReceivedMailDAO;
import com.sis.sara.ConnectionFTP.FTPClientSara;
import com.sis.sara.ConnectionFTP.SendReport;
import com.sis.sara.wdmailDAO.SubjectDAO;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Beni Barbosa
 */
public class SaraWatchDog {
    
    boolean valida = false;
    private boolean confirmNotification;
    //Variables por hora
    private int sumaCadaHora = 0;
    //diario--primera letra(d = diario)--ultima letra(c = cuenta)
    private int sumaDiarios = 0;

    //lunes (contadores)
    private int sumaLunes = 0;

    //Miercoles y viernes
    private int sumaMyV = 0;
    private int desempeñorastracc = 0;
    private int desempeñolocatekc = 0;

    private int selection = 0;


 
    
    public boolean executeProcess(int selection) {
        
         //Conectamos a Correo
        Properties prop = new Properties();
        // Deshabilitamos TLS
        prop.setProperty("mail.pop3.starttls.enable", "false");
        // Hay que usar SSL
        prop.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.setProperty("mail.pop3.socketFactory.fallback", "false");
        // Puerto 995 para conectarse.
        prop.setProperty("mail.pop3.port", "995");
        prop.setProperty("mail.pop3.socketFactory.port", "995");
        Session sesion = Session.getInstance(prop);
        // Para obtener un log más extenso.
        sesion.setDebug(false);
        try {

          Store store = sesion.getStore("pop3");
            store.connect("pop.gmail.com", "sara.reportes@gmail.com", "N7t9VNHK");
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            Message[] mensajes = folder.getMessages();

         

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(mensajes, fp);
            ArrayList subjectList=new ArrayList();

            switch (selection) {
                 case 4:
                    {
                        for (int i = 0; i < mensajes.length; i++) {
                            
                            Date receivedDate = mensajes[i].getSentDate();
                            String dateR = getHourString(receivedDate) + getDate(receivedDate);
//                            Date date = new Date();
//                            Date actualDate = new Date(date.getTime() - (3600000));
//                            String actualD = getHourString(actualDate) + getDate(actualDate);
//              
//                            
//                            if (dateR.equalsIgnoreCase("002017/08/29")||dateR.equalsIgnoreCase("012017/08/29")||
//                                    dateR.equalsIgnoreCase("022017/08/29")||dateR.equalsIgnoreCase("032017/08/29")
//                                    ||dateR.equalsIgnoreCase("042017/08/29")||dateR.equalsIgnoreCase("052017/08/29")||dateR.equalsIgnoreCase("052017/08/29")
//                                    ||dateR.equalsIgnoreCase("062017/08/29")||dateR.equalsIgnoreCase("072017/08/29")||dateR.equalsIgnoreCase("082017/08/29")
//                                    ||dateR.equalsIgnoreCase("092017/08/29")
//                                    ){

                            Date date = new Date();
                            Date hourActual=new Date(date.getTime()-7200000);                            
                            Date hourAnterior = new Date(date.getTime() - (7200000+3600000));
                            String hourAnteriorString = getHourStringComplete(hourAnterior) + getDate(hourAnterior);
                            String hour2String=getHourStringComplete(hourActual)+getDate(hourActual);
                            String dayEmail=getDate(receivedDate);
                            String dayHourActual=getDate(hourActual);
                            String dayHourAnterior=getDate(hourAnterior);
                            int hora=Integer.parseInt(getHourString(hourActual));
                            int horaAnterior=Integer.parseInt(getHourString(hourAnterior));
                            int min=Integer.parseInt(getMimnues(hourAnterior));
                            int minAterior=Integer.parseInt(getMimnues(hourActual));
                            int horaEmail=Integer.parseInt(getHourString(receivedDate));
                            int minutosEmail=Integer.parseInt(getMimnues(receivedDate));
                            if ((horaEmail==hora&&minutosEmail<50&&dayEmail.equalsIgnoreCase(dayHourActual))||
                                 (horaEmail==horaAnterior&&minutosEmail>50 &&dayEmail.equalsIgnoreCase(dayHourAnterior))){
                                System.out.println("DE: " + mensajes[i].getFrom()[0].toString());
                                System.out.println("Asunto: " + mensajes[i].getSubject());
                                System.out.println("No leidos: " + folder.getUnreadMessageCount());
                                System.out.println("Fecha: " + mensajes[i].getSentDate());
                                getCompare(mensajes[i].getSubject(), mensajes[i].getSentDate(),"HORA");
                            }
                            if(i==mensajes.length-1){
                                 
                                DeleteEmail deleteEmail =new DeleteEmail();
                                deleteEmail.delete("pop.gmail.com","pop3", "sara.reportes@gmail.com","N7t9VNHK");
                               
                            }
                        }    if (getHourString(new Date()).equals("00")) {
                            Date date = new Date(new Date().getTime() - 3600000);
                            String dateS = getDate(date);
                            //DayReportsDAO dr = new DayReportsDAO();
                            
                            
                            //dr.insert(dateS, sumaCadaHora, "cada hora");
                            sumaCadaHora = 0;
                        }       String mensaje = getPrintHourStringNew();
                        if (confirmNotification) {
                            notify(mensaje, selection);
                        }       break;
                    }
                case 1:
                    {
                        for (int i = 0; i < mensajes.length; i++) {
                            
                            Date receivedDate = mensajes[i].getSentDate();
                            String dateR = getDate(receivedDate);
                            //                    Date date = new Date();
                            Date actualDate = new Date(new Date().getTime());
                            String actualD = getDate(actualDate);
                            Date sundayDate = new Date(new Date().getTime()-(86400000));
                            String sundayString = getDate(sundayDate);
                            
                            

                            if (dateR.equalsIgnoreCase(actualD)| dateR.equalsIgnoreCase(sundayString)) {
                                System.out.println("DE: " + mensajes[i].getFrom()[0].toString());
                                System.out.println("Asunto: " + mensajes[i].getSubject());
                                System.out.println("No leidos: " + folder.getUnreadMessageCount());
                                System.out.println("Fecha: " + mensajes[i].getSentDate());
                                //Inserta el asunto en la base de datos.
                                getCompare(mensajes[i].getSubject(), mensajes[i].getSentDate(),"LUNES/DOMINGO");
                                
                            }
                                                   
                            
                        }
                        if (getHourString(new Date()).equals("12")) {
                            Date date = new Date();
                            String dateS = getDate(date);
                            DayReportsDAO dr = new DayReportsDAO();
                            dr.insert(dateS, sumaLunes, "LUNES/DOMINGO");
                            sumaLunes = 0;
                        }       String mensaje = getPrintMondayString();
                        if (confirmNotification) {
                            notify(mensaje, selection);
                        }       break;
                    }
                case 2:
                    {
                        for (int i = 0; i < mensajes.length; i++) {
                            Date receivedDate = mensajes[i].getSentDate();
                            String dateR = getDate(receivedDate);
                            Date date = new Date();
                            Date actualDate = new Date(date.getTime());
                            String actualD = getDate(actualDate);
                           
                                if (dateR.equalsIgnoreCase(actualD)) {
                                System.out.println("DE: " + mensajes[i].getFrom()[0].toString());
                                System.out.println("Asunto: " + mensajes[i].getSubject());
                                System.out.println("No leidos: " + folder.getUnreadMessageCount());
                                System.out.println("Fecha: " + mensajes[i].getSentDate());
                                getCompare(mensajes[i].getSubject(), mensajes[i].getSentDate(),"MIERCOLES/VIERNES");
                            }
                        }
                        if (getHourString(new Date()).equals("12")) {
                            Date date = new Date();
                            String dateS = getDate(date);
                            DayReportsDAO dr = new DayReportsDAO();
                            dr.insert(dateS, sumaMyV, "miercoles y viernes");
                            sumaMyV = 0;
                        }       String mensaje = getPrintWedFridayString();
                        if (confirmNotification) {
                            notify(mensaje, selection);
                        }       break;
                    }
                    case 3:
                    {
                        for (int i = 0; i < mensajes.length; i++) {
                            
                            Date receivedDate = mensajes[i].getSentDate();
                            String dateR = getDate(receivedDate);
                            Date date = new Date();
                            Date actualDate = new Date(date.getTime());
                            String actualD = getDate(actualDate);
                            if (dateR.equalsIgnoreCase(actualD)) {
                                System.out.println("DE: " + mensajes[i].getFrom()[0].toString());
                                System.out.println("Asunto: " + mensajes[i].getSubject());
                                System.out.println("No leidos: " + folder.getUnreadMessageCount());
                                System.out.println("Fecha: " + mensajes[i].getSentDate());
                                mensajes[i].getReceivedDate();
                                getCompare(mensajes[i].getSubject(), mensajes[i].getSentDate(),"DIARIO");
                            }
                        }   
                        if (getHourString(new Date()).equals("12")) {
                            Date date = new Date();
                            String dateS = getDate(date);
                            DayReportsDAO dr = new DayReportsDAO();
                            dr.insert(dateS, sumaDiarios,"DIARIO");
                            sumaDiarios = 0;
                        }       String mensaje = getPrintDayString();
                        if (confirmNotification) {
                            notify(mensaje, selection);
                        }
                        break;
                    }
                default:
                    break;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public String getHourString(Date date) {
        String fechaRetorno = "";
        DateFormat formatoFecha = new SimpleDateFormat("HH");
        fechaRetorno = formatoFecha.format(date);
        return fechaRetorno;
    }
    public String getHourStringComplete(Date date) {
        String dateReturn = "";
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateReturn = dateFormat.format(date);
        return dateReturn;
    }
     public String getDateHourStringComplete(Date date) {
        String dateReturn = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateReturn = dateFormat.format(date);
        return dateReturn;
    }
    public long getLessOneHour(Date date) {
        Date actualDate = new Date(date.getTime() - 3600000);
        long fechaRetorno = 0;
        DateFormat formatoFecha = new SimpleDateFormat("HH");
        fechaRetorno = Long.parseLong(formatoFecha.format(actualDate));//ESta en entero 
        return fechaRetorno;
    }
    public String getMimnues(Date date) {
        String dateReturn = "";
        DateFormat dateFormat = new SimpleDateFormat("mm");
        dateReturn = dateFormat.format(date);
        return dateReturn;
    }
    public String getDate(Date date) {
        String dateReturn = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        dateReturn = dateFormat.format(date);
        return dateReturn;
    }
    public int getDayOfWeekNumber(Date date) {
        String fechaRetorno = "";
        DateFormat formatoFecha = new SimpleDateFormat("u");
        fechaRetorno = formatoFecha.format(date);
        return Integer.parseInt(fechaRetorno);
    }
    public void notify(String mensaje, int selection) {
        Properties props = new Properties();
        Date dateLessHour = new Date(new Date().getTime()-7200000);
        String date = getDate(dateLessHour);
        String hour = getHourString(new Date());
        try
        {
            // Propiedades de la conexión
            Properties propiedades = new Properties();
            propiedades.setProperty("mail.smtp.host", "smtp.gmail.com");
            propiedades.setProperty("mail.smtp.starttls.enable", "true");
            propiedades.setProperty("mail.smtp.port", "587");
            propiedades.setProperty("mail.smtp.user", "sispruebas55@gmail.com");
            propiedades.setProperty("mail.smtp.auth", "true");
            propiedades.setProperty("mail.pop3.starttls.enable","false");
            propiedades.setProperty("mail.pop3.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            propiedades.setProperty("mail.pop3.socketFactory.fallback","false");
            propiedades.setProperty("mail.pop3.port","995");
            propiedades.setProperty("mail.pop3.socketFactory.port", "995");
            // Preparamos la sesion
            Session sesion = Session.getInstance(propiedades);
            sesion.setDebug(true);
            // Construimos el mensaje
            MimeMessage message = new MimeMessage(sesion);
            message.setFrom(new InternetAddress("sara.reportes@gmail.com"));
            // A quien va dirigido
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(""
                    + "medina_desarrollo@tecnologiasis.com"));
//              + "enrique_valor@tecnologiasis.com"));
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(""
             + "miguel_desarrollo@tecnologiasis.com"));
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(""
              + "benigno.desarrollosara@gmail.com"));
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(""
              + "pedro_desarrollo@tecnologiasis.com"));
            if (selection == 4) {
                Date date2 = new Date();
                long hour2 = getLessOneHour(date2);
                message.setSubject("Reportes Faltantes por HoraP: "+date+" Hora: "+hour2+" hrs.");
            } else if (selection == 1) {
                message.setSubject("Reportes Faltantes Lunes: " + date);
            } else if (selection == 2) {
                message.setSubject("Reportes Faltantes Mie/Viernes: " + date);
            } else if (selection == 3) {
                message.setSubject("Reportes Faltantes Diario: " + date);
            }
            message.setText(mensaje);
            Transport t = sesion.getTransport("smtp");
            t.connect("sara.reportes@gmail.com", "N7t9VNHK");
          //  t.connect("sara.reportes@gmail.com","N7t9VNHK");
            t.sendMessage(message, message.getAllRecipients());
            t.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getCompare(String subject, Date sentDate,String  id){
        String dateS = getDateHourStringComplete(sentDate);
        try{
            String sql = "select subject  from subject "
                    + " where subject ='"+subject+"'"
                    + " and typesubject='"+id+"'";
            System.out.println(sql);
            Statement st = DbConnection.getInstance().getConnect().createStatement();
            ResultSet rs = st.executeQuery(sql);
            if(rs.next()){
                ReceivedMailDAO rmd = new ReceivedMailDAO();
                System.out.println(rmd.insert(subject, dateS));
            }
            rs.close();
            st.close();
        }catch(Exception e){
            e.printStackTrace();
            DbConnection.getInstance().doReconnect();
            //Volver a ejecutar el proceso
            getCompare(subject,sentDate, id);
            System.out.println("entre al metodo");
        }
    }
    public String getPrintHourStringNew(){
        List<String> list = new ArrayList<String>();
        Date dateLeast1 = new Date(new Date().getTime()-3600000);
        String stringDateLeast1 = getDate(dateLeast1);
        String hourActualSQL = getHourString(dateLeast1);
        long hour = getLessOneHour(new Date());
        Date dateLeast2 = new Date(new Date().getTime()-7200000);
        String dateLeast2String=getDate(dateLeast2);
        String hourLeast2 = getHourString(dateLeast2);
        String actualDate = getDate(new Date());
        String actualHour = hour+":00:00";
        String subjectCompare= " ";
        boolean mesajeNoRecibido=false;
        boolean exist = false;
        boolean rsPrintIsNull = true;
        confirmNotification = false;
        String message = "Reportes Faltantes:\n\n";
        String var = "";
         try{
             String sql =" select SUBJECT  from SUBJECT " +
                        " WHERE TYPESUBJECT='HORA' ";
            Statement stPrint2 = DbConnection.getInstance().getConnect().createStatement();
            ResultSet rsPrint2 = stPrint2.executeQuery(sql);
            while(rsPrint2.next()){
                list.add(rsPrint2.getString(1));
            }
            stPrint2.close();
            rsPrint2.close();
        }catch(Exception e){
              System.out.println(e);
              DbConnection.getInstance().doReconnect();
        }
        for(String res: list){
            System.out.println("entrar la lista");    
            System.out.println(res);
        } 
                 try{
                 
                    String sql ="select subject.subject " +
                        "from mailreceived " +
                        "join subject on  mailreceived.Subject = subject.Subject " +
                        "where TYPESUBJECT='HORA' " +
                        "AND mailreceived.TIMESTAMP1 " +
                        "between SYSDATE-(3/24) and SYSDATE-(2/24) " +
                        "and  TypeSubject='HORA' ";
                    Statement stPrint1 = DbConnection.getInstance().getConnect().createStatement();
                    ResultSet rsPrint1 = stPrint1.executeQuery(sql);
                    while(rsPrint1.next()){
                       list.remove(rsPrint1.getString(1));
                    }
                    stPrint1.close();
                    rsPrint1.close();
            }catch(Exception e){
                e.printStackTrace();
                DbConnection.getInstance().doReconnect();
            }
                
         
        for(String res: list){
            System.out.println("no enviados");
            System.out.println(res);
        } 
        if(list.size()>0){
             for (int i = 0; i < list.size(); i++) {
                    MailNotReceivedDAO mnr = new MailNotReceivedDAO();
                    mnr.insert(list.get(i),actualDate);
                    var = subjectCompare +list.get(i);
                    message += var;
                }
                confirmNotification = true;
            
        }
        if(!mesajeNoRecibido){
            System.out.println("todos enviados");
            for(String lista:list){
                System.out.println(lista);
            }
        }
     
        return message;
    }
    //imprime asuntos
    public String getPrintDayString(){
        String actualDate = getDateHourStringComplete(new Date());
        confirmNotification = false;
        ArrayList<String> list =new ArrayList();
        ArrayList<String> list1 =new ArrayList();
        String message = "Reportes Faltantes:\n\n";
        String var = "";
        String subjectCompare="";
            
        try{
            String sql ="SELECT SUBJECT FROM SUBJECT " +
                    "WHERE TYPESUBJECT ='DIARIO'";
            Statement stPrint2 = DbConnection.getInstance().getConnect().createStatement();
            ResultSet rsPrint2 = stPrint2.executeQuery(sql);
            while(rsPrint2.next()){
                list.add(rsPrint2.getString("SUBJECT"));
            }
            stPrint2.close();
            rsPrint2.close();
        }catch(Exception e){
            e.printStackTrace();
            DbConnection.getInstance().doReconnect();
        }
        for(String res: list){
            System.out.println(res);
        } 
         try{
             String sql ="select  mailreceived.subject from mailreceived " +
                "join subject on  mailreceived.Subject = subject.Subject " +
                "where  TypeSubject='DIARIO' " +
                "and TIMESTAMP1  between SYSDATE -15/24 " +
                "and sysdate";
            Statement stPrint1 = DbConnection.getInstance().getConnect().createStatement();
            ResultSet rsPrint1 = stPrint1.executeQuery(sql);
            while(rsPrint1.next()){
                list.remove(rsPrint1.getString(1));
            }
            stPrint1.close();
            rsPrint1.close();
        }catch(Exception e){
            e.printStackTrace();
            DbConnection.getInstance().doReconnect();
        }
        if(list.size()>0){
             for (int i = 0; i < list.size(); i++) {
                    MailNotReceivedDAO mnr = new MailNotReceivedDAO();
                    mnr.insert(list.get(i),actualDate);
                    var = subjectCompare +list.get(i)+ "\n";
                    message += var;
                   /* FTPClientSara ftp=new FTPClientSara();
                    String fileName = ftp.query(list.get(i));
                    System.out.println(fileName);*/
                }
                confirmNotification = true;
            
        }
        if(list.size()==0){
            System.out.println("todos enviados");
            for(String lis:list1){
                System.out.println(lis);
            }
        }    
        return message;
    }
    public String getPrintMondayString(){
       String actualHour = getHourStringComplete(new Date());
        String actualDateFormat = getDateHourStringComplete(new Date());
        confirmNotification = false;
        ArrayList<String> list =new ArrayList();
        ArrayList<String> list1 =new ArrayList();
        String message = "Reportes Faltantes:\n\n";
        String var = "";
        String subjectCompare="";
        try{
             String sql ="SELECT SUBJECT FROM SUBJECT " +
                "WHERE TYPESUBJECT ='LUNES/DOMINGO'";
            Statement stPrint2 = DbConnection.getInstance().getConnect().createStatement();
            ResultSet rsPrint2 = stPrint2.executeQuery(sql);
            while(rsPrint2.next()){
                list.add(rsPrint2.getString("SUBJECT"));
            }
            stPrint2.close();
            rsPrint2.close();
        }catch(Exception e){
            e.printStackTrace();
            DbConnection.getInstance().doReconnect();
        }
        for(String res: list){
            System.out.println(res);
        } 
         try{
             String sql ="select  SUBJECT.SUBJECT " +
                "from MAILRECEIVED " +
                "join subject on  MAILRECEIVED.SUBJECT = subject.Subject" +
                "BETWEEN  TIMESTAMP1 BETWEEN SYSDATE -1"+
                "AND  SYSDATE " +
                "and  TypeSubject='LUNES/DOMINGO'";
            Statement stPrint1 = DbConnection.getInstance().getConnect().createStatement();
            ResultSet rsPrint1 = stPrint1.executeQuery(sql);
            while(rsPrint1.next()){
                list.remove(rsPrint1.getString(1));
            }
            stPrint1.close();
            rsPrint1.close();
        }catch(Exception e){
            e.printStackTrace();
            DbConnection.getInstance().doReconnect();
        }
        if(list.size()>0){
             for (int i = 0; i < list.size(); i++) {
                    MailNotReceivedDAO mnr = new MailNotReceivedDAO();
                    mnr.insert(list.get(i),actualDateFormat);
                    var = subjectCompare +list.get(i)+ "\n";
                    message += var;
                    /*FTPClientSara ftp=new FTPClientSara();
                    String fileName = ftp.query(list.get(i));
                    System.out.println(fileName);*/
                }
                confirmNotification = true;
        }
        if(list.size()==0){
            System.out.println("todos enviados");
            for(String lis:list1){
                System.out.println(lis);
            }
        }    
        return message;
    }
    public String getPrintWedFridayString(){
        String actualHour = getHourStringComplete(new Date());
        String actualDateFormat = getDate(new Date());
        Date yesterdayDate=new Date(new Date().getTime()-8640000);
        String yesterdayString=getDate(yesterdayDate);
        confirmNotification = false;
        ArrayList<String> list =new ArrayList();
        String message = "Reportes Faltantes:\n\n";
        String var = "";
        String subjectCompare="";
        try{
             String sql ="SELECT SUBJECT FROM SUBJECT " +
                "WHERE TYPESUBJECT ='MIERCOLES/VIERNES'";
            Statement stPrint2 = DbConnection.getInstance().getConnect().createStatement();
            ResultSet rsPrint2 = stPrint2.executeQuery(sql);
            while(rsPrint2.next()){
                list.add(rsPrint2.getString("SUBJECT"));
            }
            stPrint2.close();
            rsPrint2.close();
        }catch(Exception e){
            e.printStackTrace();
            DbConnection.getInstance().doReconnect();
        }
        for(String res: list){
            System.out.println(res);
        } 
         try{
             String sql ="select  subject.subject " +
                "from mailreceived " +
                "join subject on  mailreceived.Subject = subject.Subject " +
                "where TIMESTAMP1  between sysdate -1 " +
                "and sysdate  " +
                "and typesubject='MIERCOLES/VIERNES' ";
            Statement stPrint1 = DbConnection.getInstance().getConnect().createStatement();
            ResultSet rsPrint1 = stPrint1.executeQuery(sql);
            while(rsPrint1.next()){
                list.remove(rsPrint1.getString(1));
            }
            stPrint1.close();
            rsPrint1.close();
        }catch(Exception e){
            e.printStackTrace();
            DbConnection.getInstance().doReconnect();
        }
        if(list.size()>0){
             for (int i = 0; i < list.size(); i++) {
/*                    MailNotReceivedDAO mnr = new MailNotReceivedDAO();
                    mnr.insert(list.get(i), 
                    actualHour, actualDateFormat);
                    var = subjectCompare +list.get(i)+ "\n";
                    message += var;*/
                /*    FTPClientSara ftp=new FTPClientSara();
                    String fileName = ftp.query(list.get(i));
                    System.out.println(fileName);*/
                }
                confirmNotification = true;
            
        }
        if(list.size()==0){
            System.out.println("todos enviados");
            for(String lis:list){
                System.out.println(lis);
            }
        }    
        return message;
    
    }
    
    
    


public static void main(String[] args) {
        final SaraWatchDog wd = new SaraWatchDog();
        long delay = 0;
        long period = 3600000;
        wd.selection = 0;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {

            @Override
            public void run() {
                Date now = new Date();
                
               if (wd.getHourString(now).equals("12")) {
                    switch (wd.getDayOfWeekNumber(now) ) {
                        case 1:
                            wd.selection = 1;
                            System.out.println("entro lunes");
                            wd.executeProcess(wd.selection); // Lunes 1
                            break;
                        case 3:
                            wd.selection = 2;
                            wd.executeProcess(wd.selection); //Miercoles 2
                            break;
                        case 5:
                            wd.selection = 2;
                            wd.executeProcess(wd.selection); // Viernes 2
                            break;
                        default: ;
                            break;
                    }
                    wd.selection = 3;
                    if (!wd.executeProcess(wd.selection)) { // Dia 3
                        System.out.println("Error");
                    }
                }
                wd.selection = 4;
                if (!wd.executeProcess(wd.selection)) { //Hora 4
                    System.out.println("Error");
                }
//                wd.setValuesOfVariables();
            }

        }, delay, period);
    }
}

