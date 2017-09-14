package com.novoda.tpbot.landing;

import android.support.annotation.MenuRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.novoda.tpbot.Feature;
import com.novoda.tpbot.FeatureSelectionController;
import com.novoda.tpbot.Features;
import com.novoda.tpbot.R;

final class LandingMenuFeatureSelectionController implements FeatureSelectionController<Menu, MenuItem> {

    @MenuRes
    private static final int FEATURE_MENU_RESOURCE = R.menu.feature_menu;

    private final MenuInflater menuInflater;
    private final Features features;

    LandingMenuFeatureSelectionController(MenuInflater menuInflater, Features features) {
        this.menuInflater = menuInflater;
        this.features = features;
    }

    @Override
    public void attachFeaturesTo(Menu componentToAttachTo) {
        menuInflater.inflate(FEATURE_MENU_RESOURCE, componentToAttachTo);

        for (Integer key : features.keys()) {
            MenuItem menuItem = componentToAttachTo.findItem(key);
            Feature feature = features.get(key);
            menuItem.setChecked(feature.isEnabled());
        }
    }

    @Override
    public void handleFeatureToggle(MenuItem featureRepresentation) {
        Feature feature = features.get(featureRepresentation.getItemId());

        if (feature == null) {
            throw new IllegalStateException("You must check if data is present before using handleFeatureToggle().");
        }

        if (feature.isEnabled()) {
            featureRepresentation.setChecked(false);
            feature.setDisabled();
        } else {
            featureRepresentation.setChecked(true);
            feature.setEnabled();
        }
    }

    @Override
    public boolean contains(MenuItem featureRepresentation) {
        return features.get(featureRepresentation.getItemId()) != null;
    }

}
