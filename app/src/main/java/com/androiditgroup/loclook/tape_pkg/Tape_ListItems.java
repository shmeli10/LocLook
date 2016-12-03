package com.androiditgroup.loclook.tape_pkg;

import java.util.ArrayList;

/**
 * Created by OS1 on 29.10.2015.
 */
public class Tape_ListItems {

    private int     publicationId;
    private int     authorId;
    private boolean publicationIsFavorite;
    private boolean publicationIsLiked;

    private int     favoritePublicationRowId;
    private int     likedPublicationRowId;

    private int     userAvatar;
    private String  userName;
    private String  publicationDate;
    private int     badgeId;
    private int     badgeImage;
    private String  publicationText;
    private int     favorites;
    private String  answersSum;
    private int     answers;
    private String  likedSum;
    private int     likes;
    private int     publicationInfo;

    private float   latitude;
    private float   longitude;

    private String  regionName;
    private String  streetName;

    private ArrayList<String[]> quizAnswersList = new ArrayList<String[]>();

    public int getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(int publicationId) {
        this.publicationId = publicationId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public boolean isPublicationFavorite() {
        return publicationIsFavorite;
    }

    public void setPublicationIsFavorite(boolean publicationIsFavorite) {
        this.publicationIsFavorite = publicationIsFavorite;
    }

    public boolean isPublicationLiked() {
        return publicationIsLiked;
    }

    public void sePublicationIsLiked(boolean publicationIsLiked) {
        this.publicationIsLiked = publicationIsLiked;
    }

    public int getFavoritePublicationRowId() {
        return favoritePublicationRowId;
    }

    public void setFavoritePublicationRowId(int favoritePublicationRowId) {
        this.favoritePublicationRowId = favoritePublicationRowId;
    }

    public int getLikedPublicationRowId() {
        return likedPublicationRowId;
    }

    public void setLikedPublicationRowId(int likedPublicationRowId) {
        this.likedPublicationRowId = likedPublicationRowId;
    }

    public int getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(int userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public int getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }

    public int getBadgeImage() {
        return badgeImage;
    }

    public void setBadgeImage(int badgeImage) {
        this.badgeImage = badgeImage;
    }

    public String getPublicationText() {
        return publicationText;
    }

    public void setPublicationText(String publicationText) {
        this.publicationText = publicationText;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public String getAnswersSum() {
        return answersSum;
    }

    public void setAnswersSum(String answersSum) {
        this.answersSum = answersSum;
    }

    public int getAnswers() {
        return answers;
    }

    public void setAnswers(int answers) {
        this.answers = answers;
    }

    public String getLikedSum() {
        return likedSum;
    }

    public void setLikedSum(String likedSum) {
        this.likedSum = likedSum;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getPublicationInfo() {
        return publicationInfo;
    }

    public void setPublicationInfo(int publicationInfo) {
        this.publicationInfo = publicationInfo;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = Float.parseFloat(latitude);
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = Float.parseFloat(longitude);
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }
}