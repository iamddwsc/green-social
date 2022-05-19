package controller;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.scene.layout.AnchorPane;

public class SwapperModel {
    //this class used to get base layout
    private AnchorPane layout_swapping;
    private MaterialDesignIconView btnGo, btnBack;
    private int current_page = 0;

    private boolean flag_page_2;
    private boolean flag_page_3;

    public SwapperModel() {

    }

    public AnchorPane getLayout_swapping() {
        return layout_swapping;
    }

    public void setLayout_swapping(AnchorPane layout_swapping) {
        this.layout_swapping = layout_swapping;
    }

    public MaterialDesignIconView getBtnGo() {
        return btnGo;
    }

    public void setBtnGo(MaterialDesignIconView btnGo) {
        this.btnGo = btnGo;
    }

    public MaterialDesignIconView getBtnBack() {
        return btnBack;
    }

    public void setBtnBack(MaterialDesignIconView btnBack) {
        this.btnBack = btnBack;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public void inc_current_pane(int i) {
        this.current_page += i;
    }

    public void dec_current_pane(int i) {
        this.current_page -= i;
    }

    public boolean isFlag_page_2() {
        return flag_page_2;
    }

    public void setFlag_page_2(boolean flag_page_2) {
        this.flag_page_2 = flag_page_2;
    }

    public boolean isFlag_page_3() {
        return flag_page_3;
    }

    public void setFlag_page_3(boolean flag_page_3) {
        this.flag_page_3 = flag_page_3;
    }
}
