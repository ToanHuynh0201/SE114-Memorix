package com.example.memorix.data.remote.dto.Progress;


import com.example.memorix.model.Card;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

import java.util.List;

public class ProgressDueResponse {
    @SerializedName("due")
    private DueCards due;

    public DueCards getDue() {
        return due;
    }

    public static class DueCards {
        @SerializedName("two_sided")
        private List<Card> twoSided;

        @SerializedName("multiple_choice")
        private List<Card> multipleChoice;

        @SerializedName("fill_in_blank")
        private List<Card> fillinBlank;

        public List<Card> getTwoSided() {
            return twoSided;
        }

        public List<Card> getMultipleChoice() {
            return multipleChoice;
        }

        public List<Card> getFillinBlank() {return fillinBlank;}
    }
}
