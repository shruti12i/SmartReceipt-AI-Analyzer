const analyzeBtn = document.getElementById('analyzeBtn');
const fileInput = document.getElementById('fileInput');
const imagePreview = document.getElementById('imagePreview');

fileInput.addEventListener('change', function() {
    const file = this.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = e => {
            imagePreview.src = e.target.result;
            document.getElementById('imagePreviewContainer').classList.remove('hidden');
        }
        reader.readAsDataURL(file);
    }
});

analyzeBtn.addEventListener('click', async () => {
    const file = fileInput.files[0];
    if (!file) return alert("Please upload a receipt first.");

    // Show Loader
    document.getElementById('loader').classList.remove('hidden');
    document.getElementById('resultsContent').classList.add('hidden');
    document.getElementById('placeholder').classList.add('hidden');

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('http://localhost:8080/api/analyze', {
            method: 'POST',
            body: formData
        });
        const data = await response.json();

        if (data.status === 'success') {
            document.getElementById('totalValue').innerText = data.total_found;
            document.getElementById('rawText').innerText = data.raw_text;
            document.getElementById('resultsContent').classList.remove('hidden');
        } else {
            alert("Audit Error: " + data.message);
        }
    } catch (err) {
        alert("Server Connection Failed. Is the backend running?");
    } finally {
        document.getElementById('loader').classList.add('hidden');
    }
});