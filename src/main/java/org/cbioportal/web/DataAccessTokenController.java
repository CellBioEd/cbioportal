/*
* This file is part of cBioPortal.
*
* cBioPortal is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.cbioportal.model.DataAccessToken;
import org.cbioportal.service.DataAccessTokenService;
import org.cbioportal.service.exception.DataAccessTokenNoUserIdentityException;
import org.cbioportal.service.exception.DataAccessTokenProhibitedUserException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@InternalApi
@Validated
@Tag(name = "Data Access Tokens", description = " ")   
public class DataAccessTokenController {

    @Value("${dat.unauth_users:anonymousUser}")
    private String[] USERS_WHO_CANNOT_USE_TOKENS;

    private static String userRoleToAccessToken;
    @Value("${download_group:}") // default is empty string
    public void setUserRoleToAccessToken(String property) { userRoleToAccessToken = property; }

    @Autowired
    private DataAccessTokenService tokenService;
    private Set<String> usersWhoCannotUseTokenSet;

    @Autowired
    private void initializeUsersWhoCannotUseTokenSet() {
        usersWhoCannotUseTokenSet = new HashSet<>(Arrays.asList(USERS_WHO_CANNOT_USE_TOKENS));
    }

    private String fileName = "cbioportal_data_access_token.txt";

    @PostMapping(value = "/api/data-access-tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get all data access tokens")
    public ResponseEntity<DataAccessToken> createDataAccessToken(Authentication authentication) throws HttpClientErrorException {
        String userName = getAuthenticatedUser(authentication);
        DataAccessToken token = tokenService.createDataAccessToken(userName);
        if (token == null) {
            return new ResponseEntity<>(token, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/data-access-tokens")
    @Operation(description = "Retrieve all data access tokens")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = DataAccessToken.class))))
    public ResponseEntity<List<DataAccessToken>> getAllDataAccessTokens(HttpServletRequest request,
                                                                        Authentication authentication) {
        String userName = getAuthenticatedUser(authentication);
        List<DataAccessToken> allDataAccessTokens = tokenService.getAllDataAccessTokens(userName);
        return new ResponseEntity<>(allDataAccessTokens, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/data-access-tokens/{token}")
    @Operation(description = "Retrieve an existing data access token")
    public ResponseEntity<DataAccessToken> getDataAccessToken(
    @Parameter(required = true, description = "token") @PathVariable String token) {
        DataAccessToken dataAccessToken = tokenService.getDataAccessTokenInfo(token);
        return new ResponseEntity<>(dataAccessToken, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/api/data-access-tokens")
    @Operation(description = "Delete all data access tokens")
    public void revokeAllDataAccessTokens(Authentication authentication) {
        tokenService.revokeAllDataAccessTokens(getAuthenticatedUser(authentication));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/api/data-access-tokens/{token}")
    @Operation(description = "Delete a data access token")
    public void revokeDataAccessToken(@Parameter(required = true, description = "token") @PathVariable String token) {
        tokenService.revokeDataAccessToken(token);
    }

    // this is the entrypoint for the cBioPortal frontend to download a single user token
    @RequestMapping(method = RequestMethod.GET, value = "/api/data-access-token")
    @Operation(description = "Create a new data access token")
    public ResponseEntity<String> downloadDataAccessToken(Authentication authentication,
        HttpServletRequest request, HttpServletResponse response) throws IOException {

        // for other methods add header to trigger download of the token by the browser
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        String userName = getAuthenticatedUser(authentication);
        DataAccessToken token = tokenService.createDataAccessToken(userName);
        if (token == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(token.toString(), HttpStatus.CREATED);
    }

    private String getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new DataAccessTokenNoUserIdentityException();
        }
        String username = authentication.getName();
        if (usersWhoCannotUseTokenSet.contains(username)) {
            throw new DataAccessTokenProhibitedUserException();
        }
        if(StringUtils.isNotEmpty(userRoleToAccessToken) &&
            !authentication.getAuthorities().contains(new SimpleGrantedAuthority(userRoleToAccessToken))) {
            throw new DataAccessTokenProhibitedUserException();
        }
        return username;
    }
}
