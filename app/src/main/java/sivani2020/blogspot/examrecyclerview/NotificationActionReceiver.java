package sivani2020.blogspot.examrecyclerview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "PLAY":
                    // Handle the play action
                    break;
                case "PAUSE":
                    // Handle the pause action
                    break;
            }
        }
    }
}
