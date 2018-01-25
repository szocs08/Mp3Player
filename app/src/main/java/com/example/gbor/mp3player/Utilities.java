package com.example.gbor.mp3player;

class Utilities {

     static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString;

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage;

        percentage = ((double) currentDuration / totalDuration) * 100;

        return percentage.intValue();
    }


    static int progressToTimer(int progress, long totalDuration) {
        return (int) ((double) progress / 100 * totalDuration);
    }


}
