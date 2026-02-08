package com.umc.connext.global.oauth2.dto;

import com.umc.connext.global.oauth2.enums.SocialType;

import java.util.Map;

public class GoogleResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public SocialType getProvider() {return SocialType.GOOGLE;}

    @Override
    public String getProviderId() {return attribute.get("sub").toString();}

    @Override
    public String getEmail() {return attribute.get("email").toString();}

    @Override
    public String getName() {return attribute.get("name").toString();}
}
