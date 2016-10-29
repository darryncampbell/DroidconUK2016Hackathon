package uk.co.droidcon.hack.bstf;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class NfcItemController {

    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] geoIntentFilter;

    public enum Item {
        LASER,
        GLOCK,
        AMMO
    }

    private BehaviorSubject<Item> nfcItemSubject = BehaviorSubject.create();

    public void setupNfcAdapter(@NonNull final Context context) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        nfcPendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        final IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        intentFilter.addDataScheme("text/plain");
        geoIntentFilter = new IntentFilter[]{intentFilter};
    }

    public void onResume(@NonNull Activity activity) {
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(activity, nfcPendingIntent, geoIntentFilter, null);
        }
    }

    public void onPause(@NonNull Activity activity) {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    public void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
            final Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                final NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    final String contents = new String(msgs[i].getRecords()[0].getPayload());
                    final String itemName = contents.substring(3);
                    switch (itemName) {
                        case "laser":
                            nfcItemSubject.onNext(Item.LASER);
                            break;
                        case "glock":
                            nfcItemSubject.onNext(Item.GLOCK);
                            break;
                        case "ammo":
                            nfcItemSubject.onNext(Item.AMMO);
                            break;
                    }
                }
            }
        }
    }

    public Observable<Item> observeItemResults() {
        return nfcItemSubject.asObservable();
    }
}