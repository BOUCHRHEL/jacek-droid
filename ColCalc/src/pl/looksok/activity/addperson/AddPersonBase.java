package pl.looksok.activity.addperson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import pl.looksok.R;
import pl.looksok.activity.ColCalcActivity;
import pl.looksok.activity.addperson.utils.AddPersonUtils;
import pl.looksok.logic.CalculationLogic;
import pl.looksok.logic.PersonData;
import pl.looksok.logic.exceptions.BadInputDataException;
import pl.looksok.logic.exceptions.DuplicatePersonNameException;
import pl.looksok.utils.Constants;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public abstract class AddPersonBase extends ColCalcActivity {
	protected static final String LOG_TAG = AddPersonBase.class.getSimpleName();

	CalculationLogic calc;
	List<PersonData> inputPaysList = new ArrayList<PersonData>();
	protected AddPersonUtils utils = new AddPersonUtils();
	HashSet<String> emails = new HashSet<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getAddPersonContentView());

		calc = new CalculationLogic();
		initActivityViews();
		initButtonStyles();

		readInputBundleIfNotEmpty();
	}

	protected void initButtonStyles() {}

	protected abstract int getAddPersonContentView();

	protected void readInputBundleIfNotEmpty() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			loadInputDataFromBundle(bundle);
		}
	}

	protected void loadInputDataFromBundle(Bundle extras) {
		calc = (CalculationLogic)extras.getSerializable(Constants.BUNDLE_CALCULATION_OBJECT);
		calc.resetCalculationResult();

		for (PersonData data : calc.getInputPaysList()) {
			data.setAlreadyRefunded(0.0);
			inputPaysList.add(data);
		}
	}

	protected void initActivityViews() {
		initTopControlsBar();
	}

	private void initTopControlsBar() {
		findViewById(R.id.calc_addPerson_button).setOnClickListener(saveAndAddNextPersonClickListener);
		findViewById(R.id.calc_addMultiPerson_button).setOnClickListener(saveAndAddNextMultiPersonClickListener);
		Button mSaveAndCalculateButton = (Button)findViewById(R.id.calc_saveCalculation_button);
		mSaveAndCalculateButton.setText(R.string.addPerson_saveAndCalculate_button);
		mSaveAndCalculateButton.setOnClickListener(saveAndShowResultsClickListener);
	}

	OnClickListener saveAndShowResultsClickListener = new OnClickListener() {
		public void onClick(View v) {
			try{
				saveAndShowResults(getNewInputDataToAdd());
			}catch(BadInputDataException e){
				Log.d(LOG_TAG, "Input data was not valid: " + e.getMessage());
			}
		}
	};

	protected void saveAndShowResults(HashSet<PersonData> newInputData) {
		try{
			for (PersonData pd : newInputData) {
				inputPaysList.add(pd);
			}
			calculateAndShowResults();
		}catch(BadInputDataException e){
			Log.d(LOG_TAG, "Input data was not valid: " + e.getMessage());
		}
	}

	OnClickListener saveAndAddNextPersonClickListener = new OnClickListener() {
		public void onClick(View v) {
			try{
				saveAndAddNext(getNewInputDataToAdd(), getAddNewPersonSingleActivity());
			}catch(BadInputDataException e){
				Log.d(LOG_TAG, "Input data was not valid");
			}
		}

	};

	protected abstract Class<?> getAddNewPersonSingleActivity();

	OnClickListener saveAndAddNextMultiPersonClickListener = new OnClickListener() {
		public void onClick(View v) {
			try{
				v.requestFocus();
				HashSet<PersonData> data = getNewInputDataToAdd();
				saveAndAddNext(data, AddPersonMultiPotluck.class);
			}catch(BadInputDataException e){
				Log.d(LOG_TAG, "Bad input data");
			}
		}
	};
	
	protected void saveAndAddNext(HashSet<PersonData> newInputData, Class<?> nextActivityClass) {
		try{
			for (PersonData pd : newInputData) {
				inputPaysList.add(pd);
			}
			calc.calculate(inputPaysList);
		}catch(BadInputDataException e){
			Log.i(LOG_TAG, "Input data was not valid: " + e.getMessage());
		}finally{
			Intent intent = new Intent(getApplicationContext(), nextActivityClass) ;
			intent.putExtra(Constants.BUNDLE_CALCULATION_OBJECT, calc);
			startActivity(intent);
			overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
			finish();
		}
	}

	protected abstract HashSet<PersonData> getNewInputDataToAdd() throws BadInputDataException;

	protected void calculateAndShowResults() {
		try{
			calc.calculate(inputPaysList);
		}catch(BadInputDataException e){
			Log.d(LOG_TAG, "Bad input data provided (BadInputDataException): " + e.getMessage());
		}catch(DuplicatePersonNameException e){
			Log.d(LOG_TAG, "Bad input data provided (Duplicated person name): " + e.getMessage());
		} finally{
			Intent intent = new Intent(this.getApplicationContext(), getCalcResultActivity()) ;
			intent.putExtra(Constants.BUNDLE_CALCULATION_OBJECT, calc);
			startActivity(intent);
			overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
			finish();
		}
	}

	protected abstract Class<?> getCalcResultActivity();

	public AddPersonBase() {
		super();
	}
}