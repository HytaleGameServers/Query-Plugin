package net.hytalegameservers.query.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class QueryUpdateResponse {

    private long allowedUpdateIntervalInSeconds;
}