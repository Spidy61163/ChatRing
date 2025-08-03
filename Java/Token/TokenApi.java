package pk.edu.itu.bsai23023.chatring.Token;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TokenApi {
    @POST("generate-token")
    Call<TokenResponse> generateToken(@Body TokenRequest request);
}