package com.jelly.farmhelperv2.feature;

import com.jelly.farmhelperv2.feature.impl.*;
import com.jelly.farmhelperv2.util.LogUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class FeatureManager {
    private static FeatureManager instance;
    private final ArrayList<IFeature> features = new ArrayList<>();

    public static FeatureManager getInstance() {
        if (instance == null) {
            instance = new FeatureManager();
        }
        return instance;
    }

    @Getter
    @Setter
    private Set<IFeature> pauseExecutionFeatures = new HashSet<>();

    public List<IFeature> fillFeatures() {
        List<IFeature> featuresList = Arrays.asList(
                AutoBazaar.getInstance(),
                AntiStuck.getInstance(),
                AutoComposter.getInstance(),
                AutoCookie.getInstance(),
                AutoGodPot.getInstance(),
                AutoReconnect.getInstance(),
                AutoPestExchange.getInstance(),
                AutoRepellent.getInstance(),
                AutoSell.getInstance(),
                BanInfoWS.getInstance(),
                BPSTracker.getInstance(),
                DesyncChecker.getInstance(),
                Freelook.getInstance(),
                LagDetector.getInstance(),
                LeaveTimer.getInstance(),
                PerformanceMode.getInstance(),
                PestsDestroyer.getInstance(),
                PestsDestroyerOnTheTrack.getInstance(),
                AutoSprayonator.getInstance(),
                PetSwapper.getInstance(),
                PlotCleaningHelper.getInstance(),
                ProfitCalculator.getInstance(),
                Scheduler.getInstance(),
                UngrabMouse.getInstance(),
                VisitorsMacro.getInstance(),
                PiPMode.getInstance(),
                AutoWardrobe.instance,
                PestFarmer.instance// sue me
        );
        features.addAll(featuresList);
        return features;
    }

    public boolean shouldPauseMacroExecution() {
        return !pauseExecutionFeatures.isEmpty();
    }

    public void disableAll() {
        features.forEach(feature -> {
            if (feature.isToggled() && feature.isRunning()) {
                feature.stop();
                LogUtils.sendDebug("Disabled feature: " + feature.getName());
            }
        });
        pauseExecutionFeatures.clear();
    }

    public void disableAllExcept(IFeature... sender) {
        features.forEach(feature -> {
            if (feature.isToggled() && feature.isRunning() && !Arrays.asList(sender).contains(feature)) {
                feature.stop();
                pauseExecutionFeatures.remove(feature);
                LogUtils.sendDebug("Disabled feature: " + feature.getName());
            }
        });
    }

    public void resetAllStates() {
        features.forEach(IFeature::resetStatesAfterMacroDisabled);
    }

    public boolean isAnyOtherFeatureEnabled(IFeature sender) {
        return pauseExecutionFeatures.stream().anyMatch(f -> f != sender);
    }

    public boolean isAnyOtherFeatureEnabled(IFeature... sender) {
        return pauseExecutionFeatures.stream().anyMatch(f -> !Arrays.asList(sender).contains(f));
//        return features.stream().anyMatch(feature -> feature.shouldPauseMacroExecution() && feature.isRunning() && !Arrays.asList(sender).contains(feature));
    }

    public boolean shouldIgnoreFalseCheck() {
        if (AutoCookie.getInstance().isRunning() && !AutoCookie.getInstance().shouldCheckForFailsafes()) {
            return true;
        }
        if (AutoGodPot.getInstance().isRunning() && !AutoGodPot.getInstance().shouldCheckForFailsafes()) {
            return true;
        }
        if (AutoReconnect.getInstance().isRunning() && !AutoReconnect.getInstance().shouldCheckForFailsafes()) {
            return true;
        }
        if (PestsDestroyer.getInstance().isRunning()) {
            if (!PestsDestroyer.getInstance().shouldCheckForFailsafes()) return true;
        }
        if (PlotCleaningHelper.getInstance().isRunning() && !PlotCleaningHelper.getInstance().shouldCheckForFailsafes()) {
            return true;
        }
        if (VisitorsMacro.getInstance().isRunning() && !VisitorsMacro.getInstance().shouldCheckForFailsafes()) {
            return true;
        }
        if (AutoPestExchange.getInstance().isRunning() && !AutoPestExchange.getInstance().shouldCheckForFailsafes()) {
            return true;
        }
        if (AutoComposter.getInstance().isRunning() && !AutoComposter.getInstance().shouldCheckForFailsafes()) {
            return true;
        }
        return false;
    }

    public void enableAll() {
        features.forEach(feature -> {
            if (feature.shouldStartAtMacroStart() && feature.isToggled()) {
                feature.start();
                LogUtils.sendDebug("Enabled feature: " + feature.getName());
            }
        });
    }

    public void resume() {
        features.forEach(feature -> {
            if (feature.shouldStartAtMacroStart() && feature.isToggled()) {
                feature.resume();
                LogUtils.sendDebug("Enabled feature: " + feature.getName());
            }
        });

    }

    public void disableCurrentlyRunning(IFeature sender) {
        features.forEach(feature -> {
            if (feature.isRunning() && feature != sender) {
                feature.stop();
                LogUtils.sendDebug("Disabled feature: " + feature.getName());
            }
        });
    }

    public List<IFeature> getCurrentRunningFeatures() {
        List<IFeature> runningFeatures = new ArrayList<>();
        features.forEach(feature -> {
            if (feature.isRunning() && !feature.shouldStartAtMacroStart()) {
                runningFeatures.add(feature);
            }
        });
        return runningFeatures;
    }
}
