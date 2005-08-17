/**
 * $RCSfile: AsteriskPhoneManager.java,v $
 * $Revision: 1.13 $
 * $Date: 2005/07/02 00:22:51 $
 *
 * Copyright (C) 1999-2004 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package org.jivesoftware.phone.asterisk;

import net.sf.asterisk.manager.ManagerConnection;
import net.sf.asterisk.manager.action.*;
import net.sf.asterisk.manager.response.CommandResponse;
import net.sf.asterisk.manager.response.ManagerError;
import net.sf.asterisk.manager.response.ManagerResponse;
import org.jivesoftware.phone.*;
import static org.jivesoftware.phone.asterisk.ManagerConnectionPoolFactory.getManagerConnectionPool;
import org.jivesoftware.phone.database.PhoneDAO;
import org.jivesoftware.phone.util.PhoneConstants;
import org.jivesoftware.util.JiveConstants;
import static org.jivesoftware.util.JiveGlobals.getProperty;
import org.xmpp.packet.JID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * Asterisk dependent implementation of {@link PhoneManager}
 *
 * @author Andrew Wright
 */
@PBXInfo(make = "Asterisk", version = "1.x")
public class AsteriskPhoneManager extends BasePhoneManager implements PhoneConstants {

    private static final Logger log = Logger.getLogger(AsteriskPhoneManager.class.getName());

    private static final String DATE_FORMAT = "yyyy-MM-dd'_'HH-mm-ss";

    public AsteriskPhoneManager(PhoneDAO dao) {
        super(dao);
    }

    public void dial(String username, String extension) throws PhoneException {

        //acquire the jidUser object
        PhoneUser user = getByUsername(username);

        ManagerConnection con = null;

        try {
            con = getManagerConnectionPool().getConnection();

            PhoneDevice primaryDevice = user.getPrimaryDevice();

            OriginateAction action = new OriginateAction();
            action.setChannel(primaryDevice.getDevice());
            action.setCallerId(primaryDevice.getCallerId() != null ? primaryDevice.getCallerId() :
                    getProperty(Properties.DEFAULT_CALLER_ID));
            action.setExten(extension);
            String context = getProperty(Properties.CONTEXT, DEFAULT_CONTEXT);
            if ("".equals(context)) {
                context = DEFAULT_CONTEXT;
            }

            action.setContext(context);
            action.setPriority(1);

            ManagerResponse managerResponse = con.sendAction(action, 5 * JiveConstants.SECOND);

            if (managerResponse instanceof ManagerError) {
                log.warning(managerResponse.getMessage());
                throw new PhoneException(managerResponse.getMessage());
            }

        }
        catch (PhoneException pe) {
            throw pe;
        }
        catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new PhoneException("Unabled to dial extention " + extension, e);
        }
        finally {
            close(con);
        }

    }

    public void dial(String username, JID target) throws PhoneException {

        PhoneUser targetUser = getByUsername(target.getNode());

        if (targetUser == null) {
            throw new PhoneException("User is not configured on this server");
        }

        String extension = targetUser.getPrimaryDevice().getExtension();

        if (extension == null) {
            throw new PhoneException("User has not identified a number with himself");
        }


        dial(username, extension);
    }

    public void forward(String callSessionID, String extension) throws PhoneException {


        CallSession phoneSession = CallSessionFactory.getCallSessionFactory()
                .getPhoneSession(callSessionID);

        RedirectAction action = new RedirectAction();

        // the channel should be the person that called us
        action.setChannel(phoneSession.getLinkedChannel());
        action.setExten(extension);
        action.setPriority(1);

        String context = getProperty(Properties.CONTEXT, DEFAULT_CONTEXT);
        if ("".equals(context)) {
            context = DEFAULT_CONTEXT;
        }

        action.setContext(context);

        ManagerConnection con = null;
        try {
            con = getManagerConnectionPool().getConnection();
            ManagerResponse managerResponse = con.sendAction(action);


            if (managerResponse instanceof ManagerError) {
                log.warning(managerResponse.getMessage());
                throw new PhoneException(managerResponse.getMessage());
            }

        }
        catch (PhoneException pe) {
            throw pe;
        }
        catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new PhoneException(e.getMessage());
        }
        finally {
            close(con);
        }

    }

    public String monitor(String channel) throws PhoneException {

        String fileName = buildFileName(channel);

        MonitorAction action = new MonitorAction();
        action.setChannel(channel);
        action.setFile(fileName);
        action.setMix(true);

        ManagerConnection con = null;
        try {

            con = getManagerConnectionPool().getConnection();

            ManagerResponse managerResponse = con.sendAction(action);

            if (managerResponse instanceof ManagerError) {
                log.warning(managerResponse.getMessage());
                throw new PhoneException(managerResponse.getMessage());
            }

        }
        catch (PhoneException pe) {
            throw pe;
        }
        catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new PhoneException(e.getMessage());
        }
        finally {
            close(con);
        }

        return fileName;
    }

    public void stopMonitor(String channel) throws PhoneException {

        StopMonitorAction action = new StopMonitorAction();
        action.setChannel(channel);


        ManagerConnection con = null;
        try {

            con = getManagerConnectionPool().getConnection();

            ManagerResponse managerResponse = con.sendAction(action);

            if (managerResponse instanceof ManagerError) {
                log.warning(managerResponse.getMessage());
                throw new PhoneException(managerResponse.getMessage());
            }

        }
        catch (PhoneException pe) {
            throw pe;
        }
        catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new PhoneException(e.getMessage());
        }
        finally {
            close(con);
        }
    }

    public void invite(String callSessionID, String extension) throws PhoneException {

        CallSession phoneSession = CallSessionFactory.getCallSessionFactory()
                .getPhoneSession(callSessionID);

        RedirectAction action = new RedirectAction();

        // the channel should be the person that called us
        action.setChannel(phoneSession.getLinkedChannel());
        action.setExtraChannel(phoneSession.getChannel());
        action.setExten(extension);
        action.setPriority(1);

        String context = getProperty(Properties.CONTEXT, DEFAULT_CONTEXT);
        if ("".equals(context)) {
            context = DEFAULT_CONTEXT;
        }

        action.setContext(context);

        ManagerConnection con = null;
        try {
            con = getManagerConnectionPool().getConnection();
            ManagerResponse managerResponse = con.sendAction(action);


            if (managerResponse instanceof ManagerError) {
                log.warning(managerResponse.getMessage());
                throw new PhoneException(managerResponse.getMessage());
            }

        }
        catch (PhoneException pe) {
            throw pe;
        }
        catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new PhoneException(e.getMessage());
        }
        finally {
            close(con);
        }

    }

    public List<String> getDevices() throws PhoneException {
        List<String> devices = getSipDevices();

        Collections.sort(devices);

        // todo Add IAX support
        return devices;
    }

    @SuppressWarnings({"unchecked"})
    protected List<String> getSipDevices() throws PhoneException {

        ManagerConnection con = null;

        try {

            con = getManagerConnectionPool().getConnection();

            CommandAction action = new CommandAction();
            action.setCommand("sip show peers");

            ManagerResponse managerResponse = con.sendAction(action);
            if (managerResponse instanceof ManagerError) {
                log.warning(managerResponse.getMessage());
                throw new PhoneException(managerResponse.getMessage());
            }

            CommandResponse response = (CommandResponse) managerResponse;
            List<String> results = response.getResult();

            ArrayList<String> list = new ArrayList<String>();
            boolean isFirst = true; // The first entry is Name, we want to skip that one
            for (String result : results) {
                if (!isFirst) {
                    list.add("SIP/" + result.split("/")[0]);
                }
                isFirst = false;
            }

            return list;

        }
        catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new PhoneException(e);
        }
        finally {
            close(con);
        }

    }

    private static void close(ManagerConnection con) {

        if (con != null) {
            try {
                con.logoff();
            }
            catch (Exception e) {
                log.log(Level.SEVERE, "trouble closing connection : " + e.getMessage(), e);
            }
        }

    }

    private String buildFileName(String channel) {

        StringBuffer name = new StringBuffer(channel.replaceAll("/","-"));
        name.append("-");

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        name.append(df.format(new Date()));

        return name.toString();
    }

}