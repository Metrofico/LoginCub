package me.metrofico.logincub.objects;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class UserPremiumValidate {
    private long timeOnFinishOutPremiumEnabled;
    private long timeOnFinishJoinPremium;

    public UserPremiumValidate(long timeOnFinishOutPremiumEnabled, long timeOnFinishJoinPremium) {
        this.timeOnFinishOutPremiumEnabled = timeOnFinishOutPremiumEnabled;
        this.timeOnFinishJoinPremium = timeOnFinishJoinPremium;
    }


    public void setTimeoutPremiumEnable(int seconds) {
        if (seconds == 0) {
            timeOnFinishOutPremiumEnabled = 0;
            return;
        }
        LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(seconds, ChronoUnit.SECONDS));
        Date tmfn = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        timeOnFinishOutPremiumEnabled = tmfn.getTime();
    }

    public void setTimeoutJoinPremium(int minutes) {
        if (minutes == 0) {
            timeOnFinishJoinPremium = 0;
            return;
        }
        LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(minutes, ChronoUnit.MINUTES));
        Date tmfn = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        timeOnFinishJoinPremium = tmfn.getTime();
    }

    public boolean isFinishedPremiumValidate() {
        if (timeOnFinishJoinPremium == 0) {
            return true;
        }
        Date dFinish = new Date(timeOnFinishJoinPremium);
        Date now = new Date();
        return now.after(dFinish);
    }

    public boolean isFinishedPremiumAnswer() {
        if (timeOnFinishOutPremiumEnabled == 0) {
            return true;
        }
        Date dFinish = new Date(timeOnFinishOutPremiumEnabled);
        Date now = new Date();
        return now.after(dFinish);
    }

}
