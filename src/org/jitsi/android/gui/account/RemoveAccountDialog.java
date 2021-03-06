/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import android.app.*;
import android.content.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.account.*;

import org.jitsi.*;
import org.jitsi.android.gui.*;
import org.jitsi.service.configuration.*;

import java.util.*;

/**
 * Helper class that produces "remove account dialog". It asks the user for
 * account removal confirmation and finally removes the account.
 * Interface <tt>OnAccountRemovedListener</tt> is used to notify about account
 * removal which will not be fired if the user cancels the dialog.
 *
 * @author Pawel Domas
 */
public class RemoveAccountDialog
{
    public static AlertDialog create(Context ctx,
                                     final AccountID account,
                                     final OnAccountRemovedListener listener)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        return alert
                .setTitle(R.string.service_gui_REMOVE_ACCOUNT)
                .setMessage(
                        ctx.getString(
                                R.string.service_gui_REMOVE_ACCOUNT_MESSAGE,
                                account.getDisplayName()))
                .setPositiveButton(R.string.service_gui_YES,
                                   new DialogInterface.OnClickListener()
                   {
                       @Override
                       public void onClick(DialogInterface dialog, int which)
                       {
                           removeAccount(account);
                           listener.onAccountRemoved(account);
                           dialog.dismiss();
                       }
                   })
                .setNegativeButton(R.string.service_gui_NO,
                                   new DialogInterface.OnClickListener()
                   {
                       @Override
                       public void onClick(
                               DialogInterface dialog,
                               int which)
                       {
                           dialog.dismiss();
                       }
                   }).create();
    }

    /**
     * Removes given <tt>AccountID</tt> from the system.
     * @param accountID the account that will be uninstalled from the system.
     */
    private static void removeAccount(AccountID accountID)
    {
        ProtocolProviderFactory providerFactory =
                AccountUtils.getProtocolProviderFactory(
                        accountID.getProtocolName());

        ConfigurationService configService
                = AndroidGUIActivator.getConfigurationService();
        String prefix
                = "net.java.sip.communicator.impl.gui.accounts";
        List<String> accounts
                = configService.getPropertyNamesByPrefix(prefix, true);

        for (String accountRootPropName : accounts)
        {
            String accountUID
                    = configService.getString(accountRootPropName);

            if (accountUID.equals(accountID.getAccountUniqueID()))
            {
                configService.setProperty(accountRootPropName, null);
                break;
            }
        }

        boolean isUninstalled
                = providerFactory.uninstallAccount(accountID);

        if (!isUninstalled)
            throw new RuntimeException("Failed to uninstall account");
    }

    /**
     * Interfaces used to notify about account removal which happens after
     * the user confirms the action.
     */
    interface OnAccountRemovedListener
    {
        /**
         * Fired after <tt>accountID</tt> is removed from the system which
         * happens after user confirms the action. Will not be fired when user
         * dismisses the dialog.
         * @param accountID removed <tt>AccountID</tt>.
         */
        void onAccountRemoved(AccountID accountID);
    }

}
