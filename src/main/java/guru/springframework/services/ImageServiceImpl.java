package guru.springframework.services;

import guru.springframework.domain.Recipe;
import guru.springframework.repositories.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;

@Service
public class ImageServiceImpl implements ImageService {

    private final RecipeRepository recipeRepository;

    public ImageServiceImpl(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional
    public void saveImageFile(long id, MultipartFile file) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new RuntimeException("no recipe for this id " + id));

        try {
            Byte[] byteObject = new Byte[file.getBytes().length];

            int i =0;
            for (byte aByte : file.getBytes()) {
                byteObject[i++] = aByte;
            }
            recipe.setImage(byteObject);
            recipeRepository.save(recipe);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
