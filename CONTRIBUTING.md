# Contributing to Frisian Draught

Thank you for your interest in contributing to Frisian Draught! Please follow these guidelines to ensure a smooth collaboration and maintain the quality of the project.

---

## How to Contribute

1. **Fork the Repository:**  
   Click the "Fork" button at the top of the project page.

2. **Clone Your Fork:**  
   ```bash
   git clone https://github.com/NoamFav/frisian-draught.git
   cd frisian-draught
   ```

3. **Create a New Branch:**  
   ```bash
   git checkout -b feature/your-feature-name
   ```

4. **Make Changes:**  
   Implement your changes and commit them with descriptive messages.

5. **Push Changes:**  
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Open a Pull Request (PR):**  
   Go to the original repository, click "Pull Requests," and submit your PR with a detailed description of your changes.

---

## Code Guidelines

- Follow the existing code structure and style.
- Use meaningful variable and function names.
- Keep code modular and well-documented.
- Write concise and descriptive commit messages.
- Ensure your changes do not break existing functionality.

---

## Reporting Issues

If you encounter any issues, please check the [issue tracker](https://github.com/your-repo/frisian-draught/issues) to see if it has already been reported. If not, create a new issue with the following details:

- A clear and concise title.
- Steps to reproduce the issue.
- Expected vs. actual behavior.
- Any relevant screenshots or error messages.

---

## Pull Request Guidelines

- Ensure your code follows the established coding style.
- Reference related issues using keywords like `Fixes #123` in the PR description.
- Run tests before submitting your PR.
- Request a review from a project maintainer.
- Be responsive to feedback and ready to make improvements.

---

## Community Guidelines

- Be respectful and considerate in discussions.
- Follow the [Code of Conduct](CODE_OF_CONDUCT.md).
- Keep discussions relevant to the project.
- Help others by reviewing pull requests and answering questions.

---

## Development Setup

To set up the development environment:

1. Install Maven if you haven't already.
   ```bash
   sudo apt-get install maven  # Linux
   brew install maven         # macOS
   choco install maven        # Windows
   ```

2. Build the project using Maven:
   ```bash
   mvn clean install
   ```

3. Run the project locally:
   ```bash
   mvn javafx:run
   ```

4. Run tests:
   ```bash
   mvn test
   ```

---

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).

---

Happy coding! We appreciate your contributions to Frisian Draught.

