# Current change

## Outcome

Let visitors see the available product images on the Duke Greens product catalogue. Products without an image must continue to display a consistent visual placeholder.

## Constraints

- Product images are optional catalogue-owned presentation data; missing images are a supported state.
- Store an optional image filename with each product. Do not derive it from the product slug, name, or other identity field, even where current filenames happen to match slugs.
- The application serves image assets from `src/main/resources/static/images/`; the web layer constructs their public `/images/...` URLs.
- Keep large original product images outside the public static-resource tree. Serve only the generated 300 × 225 px PNG derivatives from `src/main/resources/static/images/300/`.
- Provide a repeatable `tools/resize-product-images.sh` utility that reads the originals, preserves their proportions without cropping, and generates centred 300 × 225 px PNG files.
- The product catalogue, including image availability and filenames, remains application-controlled. AI must not select, create, or infer product images.
- Preserve the existing product name, package detail, and price display.
- Provide meaningful alternative text for displayed product images. The placeholder must remain decorative.
- Do not add image upload, administration, external asset hosting, recommendation-page images, or multiple-image support in this slice.

## Done when

- A product with an image filename displays its matching image on the welcome-page catalogue.
- A product with no image filename displays the existing placeholder.
- A product image filename can differ from its slug without affecting rendering.
- Product cards retain their existing name, package detail, and formatted price.
- The catalogue continues to render when most products have no image.
- Automated coverage verifies both image-present and image-absent rendering.
- The resizing utility generates a 300 × 225 px PNG for each original image without exposing the originals through the application.

## Verification

- Start with focused failing MVC coverage for product cards with and without an image filename.
- Run `./mvnw verify` and `git diff --check`.

## Delivered

- Added optional, validated catalogue image filenames, seeded eleven product images, and served them from the welcome-page catalogue. Wholewheat spaghetti uses `wholewheat-spaghetti.png`, demonstrating that the filename is independent of the product slug. Product packs scale fully within their frames, which have a thin neutral-green border.
- Added MVC coverage for image-present and image-absent cards, plus domain validation coverage for optional PNG filenames.
- Added `tools/resize-product-images.sh`, which creates centred, uncropped 300 × 225 px PNG derivatives from non-public originals in `assets/product-images/original/`. Generated images are served from `src/main/resources/static/images/300/`; all sixteen current originals were generated and verified at the expected dimensions.
- Verified with `./mvnw verify`, the resizing utility, and `git diff --check`.
