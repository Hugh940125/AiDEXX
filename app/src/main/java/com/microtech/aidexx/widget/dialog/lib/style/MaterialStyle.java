package com.microtech.aidexx.widget.dialog.lib.style;

import android.content.Context;

import com.microtech.aidexx.R;
import com.microtech.aidexx.widget.dialog.lib.DialogXStyle;
import com.microtech.aidexx.widget.dialog.lib.interfaces.ProgressViewInterface;
import com.microtech.aidexx.widget.dialog.lib.views.ProgressView;

public class MaterialStyle extends DialogXStyle {
    
    public static MaterialStyle style() {
        return new MaterialStyle();
    }
    
    @Override
    public int layout(boolean light) {
        return light ? R.layout.layout_dialogx_material : R.layout.layout_dialogx_material_dark;
    }
    
    @Override
    public int enterAnimResId() {
        return R.anim.anim_dialogx_default_enter;
    }
    
    @Override
    public int exitAnimResId() {
        return R.anim.anim_dialogx_default_exit;
    }
    
    @Override
    public int[] verticalButtonOrder() {
        return new int[]{BUTTON_OK, BUTTON_OTHER, BUTTON_CANCEL};
    }
    
    @Override
    public int[] horizontalButtonOrder() {
        return new int[]{BUTTON_OTHER, SPACE, BUTTON_CANCEL, BUTTON_OK};
    }
    
    @Override
    public int splitWidthPx() {
        return 1;
    }
    
    @Override
    public int splitColorRes(boolean light) {
        return 0;
    }
    
    @Override
    public BlurBackgroundSetting messageDialogBlurSettings() {
        return null;
    }
    
    @Override
    public HorizontalButtonRes overrideHorizontalButtonRes() {
        return new HorizontalButtonRes() {
            @Override
            public int overrideHorizontalOkButtonBackgroundRes(int visibleButtonCount, boolean light) {
                return light ? R.drawable.button_dialogx_material_light : R.drawable.button_dialogx_material_night;
            }
            
            @Override
            public int overrideHorizontalCancelButtonBackgroundRes(int visibleButtonCount, boolean light) {
                return light ? R.drawable.button_dialogx_material_light : R.drawable.button_dialogx_material_night;
            }
            
            @Override
            public int overrideHorizontalOtherButtonBackgroundRes(int visibleButtonCount, boolean light) {
                return light ? R.drawable.button_dialogx_material_light : R.drawable.button_dialogx_material_night;
            }
        };
    }
    
    @Override
    public VerticalButtonRes overrideVerticalButtonRes() {
        return new VerticalButtonRes() {
            @Override
            public int overrideVerticalOkButtonBackgroundRes(int visibleButtonCount, boolean light) {
                return light ? R.drawable.button_dialogx_material_light : R.drawable.button_dialogx_material_night;
            }
            
            @Override
            public int overrideVerticalCancelButtonBackgroundRes(int visibleButtonCount, boolean light) {
                return light ? R.drawable.button_dialogx_material_light : R.drawable.button_dialogx_material_night;
            }
            
            @Override
            public int overrideVerticalOtherButtonBackgroundRes(int visibleButtonCount, boolean light) {
                return light ? R.drawable.button_dialogx_material_light : R.drawable.button_dialogx_material_night;
            }
        };
    }
    
    @Override
    public WaitTipRes overrideWaitTipRes() {
        return new WaitTipRes() {
            @Override
            public int overrideWaitLayout(boolean light) {
                return R.layout.layout_dialogx_wait;
            }
            
            @Override
            public int overrideRadiusPx() {
                return -1;
            }
            
            @Override
            public boolean blurBackground() {
                return false;
            }
            
            @Override
            public int overrideBackgroundColorRes(boolean light) {
                return 0;
            }
            
            @Override
            public int overrideTextColorRes(boolean light) {
                return light ? R.color.white : R.color.black;
            }
            
            @Override
            public ProgressViewInterface overrideWaitView(Context context, boolean light) {
                return new ProgressView(context);
            }
        };
    }
    
    @Override
    public BottomDialogRes overrideBottomDialogRes() {
        return new BottomDialogRes() {
            
            @Override
            public boolean touchSlide() {
                return true;
            }
            
            @Override
            public int overrideDialogLayout(boolean light) {
                return light ? R.layout.layout_dialogx_bottom_material : R.layout.layout_dialogx_bottom_material_dark;
            }
            
            @Override
            public int overrideMenuDividerDrawableRes(boolean light) {
                return light ? R.drawable.rect_dialogx_material_menu_split_divider : R.drawable.rect_dialogx_material_menu_split_divider_night;
            }
            
            @Override
            public int overrideMenuDividerHeight(boolean light) {
                return 1;
            }
            
            @Override
            public int overrideMenuTextColor(boolean light) {
                return light ? R.color.blackAlpha90 : R.color.whiteAlpha90;
            }
            
            @Override
            public float overrideBottomDialogMaxHeight() {
                return 0.6f;
            }
            
            @Override
            public int overrideMenuItemLayout(boolean light, int index, int count, boolean isContentVisibility) {
                return 0;
            }
            
            @Override
            public int overrideSelectionMenuBackgroundColor(boolean light) {
                return 0;
            }
            
            @Override
            public boolean selectionImageTint(boolean light) {
                return false;
            }
            
            @Override
            public int overrideSelectionImage(boolean light, boolean isSelected) {
                return isSelected ? R.drawable.img_dialogx_bottom_menu_material_item_selection : R.drawable.img_dialogx_bottom_menu_material_item_non_select;
            }
            
            @Override
            public int overrideMultiSelectionImage(boolean light, boolean isSelected) {
                return isSelected ? R.drawable.img_dialogx_bottom_menu_material_item_multi_selection : R.drawable.img_dialogx_bottom_menu_material_item_non_multi_select;
            }
            
        };
    }
    
    @Override
    public PopTipSettings popTipSettings() {
        return new PopTipSettings() {
            @Override
            public int layout(boolean light) {
                return light ? R.layout.layout_dialogx_poptip_material : R.layout.layout_dialogx_poptip_material_dark;
            }
            
            @Override
            public ALIGN align() {
                return ALIGN.BOTTOM;
            }
            
            @Override
            public int enterAnimResId(boolean light) {
                return R.anim.anim_dialogx_default_enter;
            }
            
            @Override
            public int exitAnimResId(boolean light) {
                return R.anim.anim_dialogx_default_exit;
            }
            
            @Override
            public boolean tintIcon() {
                return true;
            }
        };
    }
    
    @Override
    public PopNotificationSettings popNotificationSettings() {
        return new PopNotificationSettings() {
            @Override
            public int layout(boolean light) {
                return light ? R.layout.layout_dialogx_popnotification_material : R.layout.layout_dialogx_popnotification_material_dark;
            }
    
            @Override
            public PopNotificationSettings.ALIGN align() {
                return ALIGN.TOP;
            }
    
            @Override
            public int enterAnimResId(boolean light) {
                return R.anim.anim_dialogx_notification_enter;
            }
    
            @Override
            public int exitAnimResId(boolean light) {
                return R.anim.anim_dialogx_notification_exit;
            }
    
            @Override
            public boolean tintIcon() {
                return false;
            }
        };
    }
    
    @Override
    public PopMenuSettings popMenuSettings() {
        return new PopMenuSettings() {
            @Override
            public int layout(boolean light) {
                return light ? R.layout.layout_dialogx_popmenu_material : R.layout.layout_dialogx_popmenu_material_dark;
            }
            
            @Override
            public BlurBackgroundSetting blurBackgroundSettings() {
                return null;
            }
            
            @Override
            public int backgroundMaskColorRes() {
                return 0;
            }
            
            @Override
            public int overrideMenuDividerDrawableRes(boolean b) {
                return 0;
            }
            
            @Override
            public int overrideMenuDividerHeight(boolean b) {
                return 0;
            }
            
            @Override
            public int overrideMenuTextColor(boolean light) {
                return light ? R.color.blackAlpha90 : R.color.whiteAlpha90;
            }
            
            @Override
            public int overrideMenuItemLayoutRes(boolean b) {
                return 0;
            }
            
            @Override
            public int overrideMenuItemBackgroundRes(boolean b, int i, int i1, boolean b1) {
                return 0;
            }
            
            @Override
            public int overrideSelectionMenuBackgroundColor(boolean b) {
                return 0;
            }
            
            @Override
            public boolean selectionImageTint(boolean b) {
                return false;
            }
            
            @Override
            public int paddingVertical() {
                return 0;
            }
        };
    }
}
