package com.example.memorix.data.remote.dto.Progress;

import com.example.memorix.model.Card;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProgressUnlearnedLearnedResponse {
    @SerializedName("unlearned")
    private UnlearnedCards unlearned;

    @SerializedName("learned")
    private LearnedCards learned;

    public UnlearnedCards getUnlearned() {
        return unlearned;
    }

    public LearnedCards getLearned() {
        return learned;
    }

    public static class UnlearnedCards {
        @SerializedName("two_sided")
        private List<Card> twoSided;

        @SerializedName("multiple_choice")
        private List<Card> multipleChoice;

        @SerializedName("fill_in_blank")
        private List<Card> fillInBlank;

        public List<Card> getTwoSided() {
            return twoSided;
        }

        public List<Card> getMultipleChoice() {
            return multipleChoice;
        }

        public List<Card> getFillInBlank() {
            return fillInBlank;
        }
    }

    public static class LearnedCards {
        @SerializedName("two_sided")
        private List<Card> twoSided;

        public List<Card> getTwoSided() {
            return twoSided;
        }
    }
}
